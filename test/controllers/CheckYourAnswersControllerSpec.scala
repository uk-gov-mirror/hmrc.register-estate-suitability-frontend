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
import connectors.RegisterEstateConnector
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import pages._
import play.api.inject
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.CheckYourAnswersHelper
import views.html.CheckYourAnswersView

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase {

  "Check Your Answers Controller" must {

    "return OK and the correct view for a GET" in {

      val mockConnector = mock[RegisterEstateConnector]

      when(mockConnector.getUTRFlag()(any(), any(), any())).thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          inject
            .bind[RegisterEstateConnector]
            .toInstance(mockConnector)
        )
        .build()

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[CheckYourAnswersView]

      val helper = application.injector.instanceOf[CheckYourAnswersHelper]

      val expectedSections =
        Seq(helper.pageAnswers(emptyUserAnswers, EstateRegisteredOnlineYesNoPage.toString, Some(true))).flatten

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(expectedSections)(request, messages).toString

      application.stop()
    }

    "return OK and a section per question when the estate is not already registered" in {

      val mockConnector = mock[RegisterEstateConnector]

      when(mockConnector.getUTRFlag()(any(), any(), any())).thenReturn(Future.successful(false))

      val answers = emptyUserAnswers
        .set(DateOfDeathBeforePage, true)
        .success
        .value
        .set(MoreThanHalfMillPage, false)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(inject.bind[RegisterEstateConnector].toInstance(mockConnector))
        .build()

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)

      val result = route(application, request).value

      val view   = application.injector.instanceOf[CheckYourAnswersView]
      val helper = application.injector.instanceOf[CheckYourAnswersHelper]

      val expectedSections = Seq(
        helper.pageAnswers(answers, EstateRegisteredOnlineYesNoPage.toString, Some(false)),
        helper.pageAnswers(answers, DateOfDeathBeforePage.toString),
        helper.pageAnswers(answers, MoreThanHalfMillPage.toString),
        helper.pageAnswers(answers, MoreThanQuarterMillPage.toString),
        helper.pageAnswers(answers, MoreThanTenThousandPage.toString),
        helper.pageAnswers(answers, MoreThanTwoHalfMillPage.toString)
      ).flatten

      expectedSections.size mustBe 3

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(expectedSections)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "onSubmit" must {

      "redirect to the register an estate service when the estate is already registered" in {

        val answers = emptyUserAnswers
          .set(EstateRegisteredOnlineYesNoPage, true)
          .success
          .value
          .set(MoreThanHalfMillPage, true)
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual s"${frontendAppConfig.loginContinueUrl}/must-register-estate"

        application.stop()
      }

      "redirect to YouNeedToRegister when any answer is true" in {

        val answersWithTrue = emptyUserAnswers
          .set(MoreThanQuarterMillPage, false)
          .get
          .set(MoreThanHalfMillPage, true)
          .get
          .set(MoreThanTenThousandPage, false)
          .get

        val application = applicationBuilder(userAnswers = Some(answersWithTrue)).build()

        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.YouNeedToRegisterController.onPageLoad().url

        application.stop()
      }

      "redirect to DoNotNeedToRegister when no answer is true" in {

        val answersWithoutTrue = emptyUserAnswers
          .set(MoreThanQuarterMillPage, false)
          .get
          .set(MoreThanHalfMillPage, false)
          .get
          .set(MoreThanTenThousandPage, false)
          .get

        val application = applicationBuilder(userAnswers = Some(answersWithoutTrue)).build()

        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.DoNotNeedToRegisterController.onPageLoad().url

        application.stop()
      }

      "redirect to DoNotNeedToRegister when all answers are missing" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.DoNotNeedToRegisterController.onPageLoad().url

        application.stop()
      }
    }

  }

}
