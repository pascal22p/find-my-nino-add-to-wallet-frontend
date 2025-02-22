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
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AuthConnector
import util.Fixtures.buildFakeRequestWithAuth
import views.html.public.SessionTimeoutView

class PublicControllerSpec extends SpecBase {

  private def controller = new PublicController(injected[SessionTimeoutView],injected[AuthConnector])(
    configDecorator,cc,config,env)

  "Calling PublicController.sessionTimeout" - {

    "return 200" in {

      val r = controller.sessionTimeout(buildFakeRequestWithAuth("GET"))
      status(r) mustBe OK
    }
  }
}
