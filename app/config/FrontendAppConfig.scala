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

package config

import com.google.inject.{Inject, Singleton}
import controllers.bindable.Origin
import play.api.Configuration
import play.api.i18n.Lang
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration, servicesConfig: ServicesConfig) {

  val host: String    = configuration.get[String]("host")
  val appName: String = configuration.get[String]("appName")

  private val contactHost = configuration.get[String]("contact-frontend.host")
  private val contactFormServiceIdentifier = "save-your-national-insurance-number"

  lazy val gtmContainer: String = configuration.get[String]("tracking-consent-frontend.gtm.container")


  private def getExternalUrl(key: String): Option[String] =
    configuration.getOptional[String](s"external-url.$key")

  def feedbackUrl(implicit request: RequestHeader): String =
    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${SafeRedirectUrl(host + request.uri).encodedUrl}"

  val loginUrl: String                  = configuration.get[String]("urls.login")
  val loginContinueUrl: String          = configuration.get[String]("urls.loginContinue")
  val signOutUrl: String                = configuration.get[String]("urls.signOut")
  lazy val findMyNinoServiceUrl: String = servicesConfig.baseUrl("find-my-nino-add-to-wallet-service")

  lazy val pertaxFrontendAuthHost: String = getExternalUrl("pertax-frontend.auth-host").getOrElse("")

  private val exitSurveyBaseUrl: String = getExternalUrl(s"feedback-survey-frontend.host").getOrElse("")
  val exitSurveyUrl: String             = s"$exitSurveyBaseUrl/feedback/save-your-national-insurance-number"

  val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("features.welsh-translation")

  def languageMap: Map[String, Lang] = Map(
    "en" -> Lang("en"),
    "cy" -> Lang("cy")
  )

  val timeout: Int   = configuration.get[Int]("timeout-dialog.timeout")
  val countdown: Int = configuration.get[Int]("timeout-dialog.countdown")

  val cacheTtl: Int = configuration.get[Int]("mongodb.timeToLiveInSeconds")

  lazy val SCAWrapperEnabled = configuration.getOptional[Boolean]("features.sca-wrapper-enabled").getOrElse(false)
  lazy val govukWalletEnabled = configuration.getOptional[Boolean]("features.govuk-wallet-enabled").getOrElse(false)

  lazy val basGatewayFrontendHost: String     = getExternalUrl(s"bas-gateway-frontend.host").getOrElse("")
  lazy val multiFactorAuthenticationUpliftUrl = s"$basGatewayFrontendHost/bas-gateway/uplift-mfa"

  lazy val origin: String = configuration.getOptional[String]("sosOrigin").orElse(Some(appName)).getOrElse("undefined")

  lazy val personalAccount = "/personal-account"
  private lazy val identityVerificationHost: String = getExternalUrl(s"identity-verification.host").getOrElse("")
  private lazy val identityVerificationPrefix: String = getExternalUrl(s"identity-verification.prefix").getOrElse("mdtp")
  lazy val identityVerificationUpliftUrl = s"$identityVerificationHost/$identityVerificationPrefix/uplift"
  val defaultOrigin: Origin = Origin("STORE_MY_NINO")
  lazy val saveYourNationalNumberFrontendHost: String = getExternalUrl(s"save-your-national-insurance-number-frontend.host").getOrElse("")

  private lazy val taxEnrolmentAssignmentFrontendHost: String = getExternalUrl(s"tax-enrolment-assignment-frontend.host").getOrElse("")

  def getTaxEnrolmentAssignmentRedirectUrl(url: String): String =
    s"$taxEnrolmentAssignmentFrontendHost/protect-tax-info?redirectUrl=${SafeRedirectUrl(url).encodedUrl}"

}
