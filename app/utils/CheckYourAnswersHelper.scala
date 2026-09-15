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

package utils

import com.google.inject.Inject
import config.FrontendAppConfig
import models.UserAnswers
import pages._
import play.api.i18n.Messages
import viewmodels.{AnswerRow, AnswerSection}

class CheckYourAnswersHelper @Inject() (
  answerRowConverter: AnswerRowConverter,
  config: FrontendAppConfig
) {

  def pageAnswers(
    userAnswers: UserAnswers,
    pageName: String,
    answerOverride: Option[Boolean] = None
  )(implicit messages: Messages): Option[AnswerSection] = {

    val page        = yesNoPageForRegister(pageName)
    val changeRoute = changeRouteForTaxYear(pageName)

    val answersToDisplay: UserAnswers =
      answerOverride match {
        case Some(value) =>
          userAnswers
            .set(page, value)
            .getOrElse(userAnswers)

        case None =>
          userAnswers
      }

    val bound = answerRowConverter.bind(answersToDisplay)

    val answerRows: Seq[AnswerRow] = Seq(
      bound.yesNoQuestion(
        page,
        pageName,
        changeRoute
      )
    ).flatten

    answerRows match {
      case Nil =>
        None

      case _ =>
        Some(
          AnswerSection(
            Some(
              messages(
                "taxLiabilityBetweenYears.checkYourAnswerSectionHeading",
                pageName
              )
            ),
            answerRows
          )
        )
    }
  }

  private def changeRouteForTaxYear(pageName: String): String =
    pageName match {
      case "haveUtrYesNo"        => s"${config.loginContinueUrl}/have-utr?origin=suitability-check-your-answers"
      case "dateOfDeathBefore"   => controllers.routes.DateOfDeathBeforeController.onPageLoad().url
      case "moreThanHalfMill"    => controllers.routes.MoreThanHalfMillController.onPageLoad().url
      case "moreThanQuarterMill" => controllers.routes.MoreThanQuarterMillController.onPageLoad().url
      case "moreThanTenThousand" => controllers.routes.MoreThanTenThousandController.onPageLoad().url
      case "moreThanTwoHalfMill" => controllers.routes.MoreThanTwoHalfMillController.onPageLoad().url
    }

  private def yesNoPageForRegister(
    pageName: String
  ): QuestionPage[Boolean] =
    pageName match {
      case "haveUtrYesNo"        => EstateRegisteredOnlineYesNoPage
      case "dateOfDeathBefore"   => DateOfDeathBeforePage
      case "moreThanHalfMill"    => MoreThanHalfMillPage
      case "moreThanQuarterMill" => MoreThanQuarterMillPage
      case "moreThanTenThousand" => MoreThanTenThousandPage
      case "moreThanTwoHalfMill" => MoreThanTwoHalfMillPage
    }

}
