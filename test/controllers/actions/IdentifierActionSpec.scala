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

package controllers.actions

import base.SpecBase
import config.FrontendAppConfig
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import play.api.mvc.{Action, AnyContent, BodyParsers, Results}
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

class IdentifierActionSpec extends SpecBase {

  type RetrievalType = Option[String] ~ Option[AffinityGroup]

  val mockAuthConnector: AuthConnector = Mockito.mock(classOf[AuthConnector])
  val appConfig: FrontendAppConfig     = injector.instanceOf[FrontendAppConfig]

  class Harness(authAction: IdentifierAction) {
    def onPageLoad(): Action[AnyContent] = authAction(_ => Results.Ok)
  }

  class ThrowingHarness(authAction: IdentifierAction) {
    def onPageLoad(): Action[AnyContent] = authAction(_ => throw new RuntimeException("Thrown by test"))
  }

  lazy val trustsAuth = new TrustsAuthorisedFunctions(mockAuthConnector, appConfig)

  private def authRetrievals(affinityGroup: AffinityGroup): Future[Some[String] ~ Some[AffinityGroup]] =
    Future.successful(new ~(Some("id"), Some(affinityGroup)))

  "invoking an AuthenticatedIdentifier" when {

    "an Agent" must {

      "allow user to continue" in {

        val application = applicationBuilder(userAnswers = None).build()

        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]

        when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any()))
          .thenReturn(authRetrievals(AffinityGroup.Agent))

        val action = new AuthenticatedIdentifierAction(trustsAuth, bodyParsers)

        val controller = new Harness(action)
        val result     = controller.onPageLoad()(fakeRequest)

        status(result) mustBe OK
        application.stop()
      }
    }

    "Org user with no enrolments" must {
      "allow user to continue" in {

        val application = applicationBuilder(userAnswers = None).build()

        when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any()))
          .thenReturn(authRetrievals(AffinityGroup.Organisation))

        val action = new AuthenticatedIdentifierAction(trustsAuth, bodyParsers)

        val controller = new Harness(action)
        val result     = controller.onPageLoad()(fakeRequest)

        status(result) mustBe OK
        application.stop()
      }
    }

    "a user with no internal id" must {
      "redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any()))
          .thenReturn(Future.successful(new ~(None, Some(AffinityGroup.Organisation))))

        val action = new AuthenticatedIdentifierAction(trustsAuth, bodyParsers)

        val controller = new Harness(action)
        val result     = controller.onPageLoad()(fakeRequest)

        status(result)           mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.UnauthorisedController.onPageLoad.url)

        application.stop()
      }
    }

    "Individual user" must {
      "redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        when(
          mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any())
        ) thenReturn (Future failed UnsupportedAffinityGroup())

        val action = new AuthenticatedIdentifierAction(trustsAuth, bodyParsers)

        val controller = new Harness(action)
        val result     = controller.onPageLoad()(fakeRequest)

        status(result)           mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.UnauthorisedController.onPageLoad.url)

        application.stop()
      }
    }

    "the user hasn't logged in" must {
      "redirect the user to log in " in {

        val application = applicationBuilder(userAnswers = None).build()

        when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any()))
          .thenReturn(Future.failed(MissingBearerToken()))

        val action = new AuthenticatedIdentifierAction(trustsAuth, bodyParsers)

        val controller = new Harness(action)
        val result     = controller.onPageLoad()(fakeRequest)

        status(result) mustBe SEE_OTHER

        redirectLocation(result).get must startWith(frontendAppConfig.loginUrl)
        application.stop()
      }
    }

    "the user's session has expired" must {
      "redirect the user to log in " in {

        val application = applicationBuilder(userAnswers = None).build()

        when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any()))
          .thenReturn(Future.failed(BearerTokenExpired()))

        val action = new AuthenticatedIdentifierAction(trustsAuth, bodyParsers)

        val controller = new Harness(action)
        val result     = controller.onPageLoad()(fakeRequest)

        status(result) mustBe SEE_OTHER

        redirectLocation(result).get must startWith(frontendAppConfig.loginUrl)
        application.stop()
      }
    }

    "the invoked block throws an exception" must {
      "propagate the exception" in {

        val application = applicationBuilder(userAnswers = None).build()

        when(mockAuthConnector.authorise(any(), any[Retrieval[RetrievalType]]())(any(), any()))
          .thenReturn(authRetrievals(AffinityGroup.Organisation))

        val action = new AuthenticatedIdentifierAction(trustsAuth, bodyParsers)

        val controller = new ThrowingHarness(action)
        val result     = controller.onPageLoad()(fakeRequest)

        assertThrows[RuntimeException](status(result))
        application.stop()
      }
    }
  }

}
