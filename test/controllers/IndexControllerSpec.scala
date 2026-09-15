/*
 * Copyright 2026 HM Revenue & Customs
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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository

import scala.concurrent.Future

class IndexControllerSpec extends SpecBase {

  val mockSessionRepository: SessionRepository = Mockito.mock(classOf[SessionRepository])

  "Index Controller" must {

    "redirect for an existing session" in {

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val request = FakeRequest(GET, routes.IndexController.onPageLoad(None).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe routes.DateOfDeathBeforeController.onPageLoad().url

      application.stop()
    }

    "create a new session and redirect" in {

      val application = applicationBuilder(userAnswers = None)
        .build()

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val request = FakeRequest(GET, routes.IndexController.onPageLoad(None).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe routes.DateOfDeathBeforeController.onPageLoad().url

      application.stop()
    }

    "redirect to check your answers when an origin is supplied" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, routes.IndexController.onPageLoad(Some("suitability")).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe routes.CheckYourAnswersController.onPageLoad.url

      application.stop()
    }
  }

}
