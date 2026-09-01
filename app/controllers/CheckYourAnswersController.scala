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

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.RegisterEstateConnector
import controllers.actions.RegisterEstateActions
import pages._
import play.api.Logging
import play.api.http.HeaderNames
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CheckYourAnswersHelper
import views.html.CheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  val controllerComponents: MessagesControllerComponents,
  checkYourAnswersView: CheckYourAnswersView,
  checkYourAnswersHelper: CheckYourAnswersHelper,
  sessionRepository: SessionRepository,
  actions: RegisterEstateActions,
  val appConfig: FrontendAppConfig,
  registerEstateConnector: RegisterEstateConnector
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(): Action[AnyContent] = actions.authWithData.async { implicit request =>
    val hcWithCookie = hc.copy(extraHeaders = hc.headers(Seq(HeaderNames.COOKIE)))

    for {
      utrFlag        <- registerEstateConnector.getUTRFlag()(request, hcWithCookie, ec)
      updatedAnswers <- Future.fromTry(request.userAnswers.set(EstateRegisteredOnlineYesNoPage, utrFlag))
      _              <- sessionRepository.set(updatedAnswers)
    } yield {
      val pages: Seq[(String, Option[Boolean])] =
        if (utrFlag) {
          Seq(EstateRegisteredOnlineYesNoPage.toString -> Some(utrFlag))
        } else {
          Seq(
            EstateRegisteredOnlineYesNoPage.toString -> Some(utrFlag),
            DateOfDeathBeforePage.toString           -> None,
            MoreThanHalfMillPage.toString            -> None,
            MoreThanQuarterMillPage.toString         -> None,
            MoreThanTenThousandPage.toString         -> None,
            MoreThanTwoHalfMillPage.toString         -> None
          )
        }

      val sections =
        pages.flatMap { case (pageName, answerOverride) =>
          checkYourAnswersHelper.pageAnswers(
            request.userAnswers,
            pageName,
            answerOverride
          )
        }

      Ok(checkYourAnswersView(sections))

    }
  }

  def onSubmit(): Action[AnyContent] = actions.authWithData.async { implicit request =>
    val isAlreadyRegistered = request.userAnswers.get(EstateRegisteredOnlineYesNoPage).contains(true)

    val answers = Seq(
      request.userAnswers.get(MoreThanQuarterMillPage),
      request.userAnswers.get(MoreThanHalfMillPage),
      request.userAnswers.get(MoreThanTenThousandPage),
      request.userAnswers.get(MoreThanTwoHalfMillPage)
    )

    val needsToRegister = answers.flatten.contains(true)

    val result: Result =
      if (isAlreadyRegistered) {
        Redirect(s"${appConfig.loginContinueUrl}/must-register-estate")
      } else if (needsToRegister) {
        Redirect(controllers.routes.YouNeedToRegisterController.onPageLoad())
      } else {
        Redirect(controllers.routes.DoNotNeedToRegisterController.onPageLoad())
      }

    Future.successful(result)
  }

}
