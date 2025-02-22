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

import base.SpecBase
import connectors.{StoreMyNinoConnector, CitizenDetailsConnector, PersonDetailsSuccessResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.libs.json.{JsString, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import util.CDFixtures
import util.Stubs.userLoggedInFMNUser
import util.TestData.NinoUser
import views.html.print.PrintNationalInsuranceNumberView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NinoLetterControllerSpec extends SpecBase with CDFixtures with MockitoSugar {

  val pd = buildPersonDetails
  val jsonPd: JsString = JsString(Json.toJson(pd).toString())

  val personDetailsId = "pdId"

  lazy val mockApplePassConnector = mock[StoreMyNinoConnector]
  lazy val mockCitizenDetailsConnector = mock[CitizenDetailsConnector]
  lazy val ninoLetterController = applicationWithConfig.injector.instanceOf[NinoLetterController]
  lazy val view = applicationWithConfig.injector.instanceOf[PrintNationalInsuranceNumberView]

  when(mockCitizenDetailsConnector.personDetails(any())(any()))
    .thenReturn(Future(PersonDetailsSuccessResponse(buildPersonDetails)))

  when(mockApplePassConnector.getPersonDetails(any())(any(), any()))
    .thenReturn(Future.successful(Some(jsonPd.toString())))

  "NinoLetter Controller" - {
    "must return OK and the correct view for a GET" in {
      userLoggedInFMNUser(NinoUser)
0
      val application = applicationBuilderWithConfig()
        .overrides(
          bind[StoreMyNinoConnector].toInstance(mockApplePassConnector),
          bind[CitizenDetailsConnector].toInstance(mockCitizenDetailsConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.NinoLetterController.onPageLoad.url)
          .withSession(("authToken", "Bearer 123"))

        val result = route(application, request).value
        status(result) mustEqual OK
      }
    }
  }

  "NinoLetterController saveNationalInsuranceNumberAsPdf" - {
    "must return OK and pdf file with correct content" in {
      userLoggedInFMNUser(NinoUser)

      val application = applicationBuilderWithConfig()
        .overrides(
          bind[StoreMyNinoConnector].toInstance(mockApplePassConnector),
          bind[CitizenDetailsConnector].toInstance(mockCitizenDetailsConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.NinoLetterController.saveNationalInsuranceNumberAsPdf.url)
          .withSession(("authToken", "Bearer 123"))

        val result = route(application, request).value
        status(result) mustEqual OK
      }
    }
  }
}
