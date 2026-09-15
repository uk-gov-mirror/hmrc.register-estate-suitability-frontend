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

import base.SpecBase
import pages._

class CheckYourAnswersHelperSpec extends SpecBase {

  private lazy val helper: CheckYourAnswersHelper = injector.instanceOf[CheckYourAnswersHelper]

  private val changeUrls = Map(
    EstateRegisteredOnlineYesNoPage.toString ->
      s"${frontendAppConfig.loginContinueUrl}/have-utr?origin=suitability-check-your-answers",
    DateOfDeathBeforePage.toString           -> controllers.routes.DateOfDeathBeforeController.onPageLoad().url,
    MoreThanHalfMillPage.toString            -> controllers.routes.MoreThanHalfMillController.onPageLoad().url,
    MoreThanQuarterMillPage.toString         -> controllers.routes.MoreThanQuarterMillController.onPageLoad().url,
    MoreThanTenThousandPage.toString         -> controllers.routes.MoreThanTenThousandController.onPageLoad().url,
    MoreThanTwoHalfMillPage.toString         -> controllers.routes.MoreThanTwoHalfMillController.onPageLoad().url
  )

  private val pages: Map[String, QuestionPage[Boolean]] = Map(
    EstateRegisteredOnlineYesNoPage.toString -> EstateRegisteredOnlineYesNoPage,
    DateOfDeathBeforePage.toString           -> DateOfDeathBeforePage,
    MoreThanHalfMillPage.toString            -> MoreThanHalfMillPage,
    MoreThanQuarterMillPage.toString         -> MoreThanQuarterMillPage,
    MoreThanTenThousandPage.toString         -> MoreThanTenThousandPage,
    MoreThanTwoHalfMillPage.toString         -> MoreThanTwoHalfMillPage
  )

  "pageAnswers" must {

    "return nothing when the page has not been answered" in
      pages.keys.foreach { pageName =>
        helper.pageAnswers(emptyUserAnswers, pageName) mustBe None
      }

    pages.foreach { case (pageName, page) =>
      s"build a section from the stored answer for $pageName" in {

        val answers = emptyUserAnswers.set(page, true).success.value

        val section = helper.pageAnswers(answers, pageName).value

        section.headingKey.value mustBe
          messages("taxLiabilityBetweenYears.checkYourAnswerSectionHeading", pageName)

        section.rows.size                 mustBe 1
        section.rows.head.label           mustBe messages(s"$pageName.checkYourAnswersLabel")
        section.rows.head.answer.toString mustBe messages("site.yes")
        section.rows.head.changeUrl       mustBe changeUrls(pageName)
      }

      s"prefer the override over the stored answer for $pageName" in {

        val answers = emptyUserAnswers.set(page, true).success.value

        val section = helper.pageAnswers(answers, pageName, answerOverride = Some(false)).value

        section.rows.head.answer.toString mustBe messages("site.no")
      }

      s"build a section from the override alone for $pageName" in {

        val section = helper.pageAnswers(emptyUserAnswers, pageName, answerOverride = Some(true)).value

        section.rows.head.answer.toString mustBe messages("site.yes")
      }
    }
  }

}
