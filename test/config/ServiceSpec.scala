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

import base.SpecBase
import config.Service.convertToString
import org.scalatestplus.mockito.MockitoSugar

class ServiceSpec extends SpecBase with MockitoSugar{
   "Service" - {

     "test baseUrl value" in {
       val service = Service("localhost", "1111", "http")
       service.baseUrl mustBe  "http://localhost:1111"
       service.toString mustBe  "http://localhost:1111"
       convertToString(service) mustBe  "http://localhost:1111"
     }

   }
}
