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

package base

import config.ConfigDecorator
import controllers.actions._
import models.UserAnswers
import org.jsoup.Jsoup
import org.scalactic.source.Position
import org.scalatest.Assertion
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{Application, Configuration, Environment}
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{MessagesControllerComponents, RequestHeader}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import uk.gov.hmrc.sca.connectors.ScaWrapperDataConnector
import uk.gov.hmrc.sca.models.{MenuItemConfig, PtaMinMenuConfig, WrapperDataResponse}
import util.WireMockSupport

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

class SpecBase extends WireMockSupport with MockitoSugar with GuiceOneAppPerSuite {

  implicit lazy val application: Application = applicationBuilder().build()
  implicit lazy val applicationWithConfig: Application = applicationBuilderWithConfig().build()
  implicit val hc: HeaderCarrier         = HeaderCarrier()
  val userAnswersId: String = "id"

  def emptyUserAnswers : UserAnswers = UserAnswers(userAnswersId)

  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())


  def messages(app: Application, request: RequestHeader): Messages =
    app.injector.instanceOf[MessagesApi].preferred(request)

  protected implicit def hc(implicit rh: RequestHeader): HeaderCarrier =
    HeaderCarrierConverter.fromRequestAndSession(rh, rh.session)

  protected def applicationBuilderWithConfig (
                                    config:      Map[String, Any] = Map(),
                                    userAnswers: Option[UserAnswers] = None): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        config ++ Map(
          "microservice.services.auth.port" -> wiremockPort,
          "microservice.host" -> "http://localhost:9900/fmn"
        )
      )
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers))
      )

  protected def applicationBuilder(
                                    userAnswers: Option[UserAnswers] = None): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers))
      )

  protected def assertSameHtmlAfter(
                                     transformation: String => String
                                   )(left: String, right: String)(implicit position: Position): Assertion = {
    val leftHtml = Jsoup.parse(transformation(left))
    val rightHtml = Jsoup.parse(transformation(right))
    leftHtml.html() mustBe rightHtml.html()
  }

  def injected[T](c: Class[T]): T = app.injector.instanceOf(c)

  def injected[T](implicit evidence: ClassTag[T]): T = app.injector.instanceOf[T]

  implicit lazy val ec = app.injector.instanceOf[ExecutionContext]

  lazy val configDecorator = app.injector.instanceOf[ConfigDecorator]

  lazy val config = app.injector.instanceOf[Configuration]

  implicit lazy val cc = app.injector.instanceOf[MessagesControllerComponents]

  implicit lazy val env = app.injector.instanceOf[Environment]
  def buildFakeRequestWithSessionId(method: String) =
    FakeRequest(method, "/save-your-national-insurance-number").withSession("sessionId" -> "FAKE_SESSION_ID")

  val mockScaWrapperDataConnector = mock[ScaWrapperDataConnector]
  val wrapperDataResponse = WrapperDataResponse(Seq(MenuItemConfig("sample-text", "sample-href", leftAligned = false,
    position = 0, None, None)), PtaMinMenuConfig(menuName = "sample-menu", backName = "sample-back-name"))

}


