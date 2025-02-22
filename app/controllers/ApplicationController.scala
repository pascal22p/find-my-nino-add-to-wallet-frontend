/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import com.google.inject.Inject
import config.{ConfigDecorator, FrontendAppConfig}
import controllers.bindable.Origin
import play.api.{Configuration, Environment}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.{IdentityVerificationFrontendService, InsufficientEvidence, Success, UserAborted, _}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl.idFunctor
import uk.gov.hmrc.play.bootstrap.binders.{OnlyRelative, RedirectUrl, SafeRedirectUrl}

import scala.concurrent.{ExecutionContext, Future}
import views.html.identity._

class ApplicationController @Inject()(
  val identityVerificationFrontendService: IdentityVerificationFrontendService,
  authConnector: AuthConnector,
  successView: SuccessView,
  cannotConfirmIdentityView: CannotConfirmIdentityView,
  failedIvIncompleteView: FailedIvIncompleteView,
  lockedOutView: LockedOutView,
  timeOutView: TimeOutView,
  technicalIssuesView: TechnicalIssuesView
)(implicit config: Configuration,
  configDecorator: ConfigDecorator,
  env: Environment,
  ec: ExecutionContext,
  cc: MessagesControllerComponents,
  frontendAppConfig: FrontendAppConfig)
  extends FMNBaseController(authConnector) with I18nSupport {

  def uplift(redirectUrl: Option[SafeRedirectUrl]): Action[AnyContent] = Action.async {
    Future.successful(Redirect(redirectUrl.map(_.url).getOrElse(routes.StoreMyNinoController.onPageLoad.url)))
  }

  def showUpliftJourneyOutcome(continueUrl: Option[SafeRedirectUrl]): Action[AnyContent] =
    Action.async { implicit request =>
      val journeyId =
        List(request.getQueryString("token"), request.getQueryString("journeyId")).flatten.headOption

      val retryUrl = routes.ApplicationController.uplift(continueUrl).url

      journeyId match {
        case Some(jid) =>
          identityVerificationFrontendService
            .getIVJourneyStatus(jid)
            .map {
              case Success =>
                Ok(successView(continueUrl.map(_.url).getOrElse(routes.StoreMyNinoController.onPageLoad.url)))

              case InsufficientEvidence =>
                Unauthorized(cannotConfirmIdentityView(retryUrl))

              case UserAborted =>
                logErrorMessage(UserAborted.toString)
                Unauthorized(cannotConfirmIdentityView(retryUrl))

              case FailedMatching =>
                logErrorMessage(FailedMatching.toString)
                Unauthorized(cannotConfirmIdentityView(retryUrl))

              case Incomplete =>
                logErrorMessage(Incomplete.toString)
                Unauthorized(failedIvIncompleteView(retryUrl))

              case PrecondFailed =>
                logErrorMessage(PrecondFailed.toString)
                Unauthorized(cannotConfirmIdentityView(retryUrl))

              case LockedOut =>
                logErrorMessage(LockedOut.toString)
                Unauthorized(lockedOutView(allowContinue = false))

              case Timeout =>
                logErrorMessage(Timeout.toString)
                Unauthorized(timeOutView(retryUrl))

              case TechnicalIssue =>
                logErrorMessage(s"TechnicalIssue response from IdentityVerificationFrontendService")
                FailedDependency(technicalIssuesView(retryUrl))

              case _ =>
                logErrorMessage("unknown status from IdentityVerificationFrontendService")
                FailedDependency(technicalIssuesView(retryUrl))
            }
            .getOrElse(BadRequest(technicalIssuesView(retryUrl)))
        case _         =>
          logErrorMessage("journeyId missing or incorect")
          Future.successful(FailedDependency(technicalIssuesView(retryUrl)))
      }
    }

  private def logErrorMessage(reason: String): Unit =
    logger.warn(s"Unable to confirm user identity: $reason")

  def signout(continueUrl: Option[RedirectUrl], origin: Option[Origin]): Action[AnyContent] =
    Action {
      val safeUrl = continueUrl.flatMap { redirectUrl =>
        redirectUrl.getEither(OnlyRelative) match {
          case Right(safeRedirectUrl) => Some(safeRedirectUrl.url)
          case _                      => Some(configDecorator.getFeedbackSurveyUrl(configDecorator.defaultOrigin))
        }
      }
      safeUrl
        .orElse(origin.map(configDecorator.getFeedbackSurveyUrl))
        .fold(BadRequest("Missing origin")) { url: String =>
          Redirect(configDecorator.getBasGatewayFrontendSignOutUrl(url))
        }
    }
}

