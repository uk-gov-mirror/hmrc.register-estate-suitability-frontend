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
import play.twirl.api.Html
import viewmodels.{AnswerRow, AnswerSection, RepeaterAnswerSection}

class SectionFormatterSpec extends SpecBase {

  private val row = AnswerRow("site.yes", Html("Yes"), "/change-me")

  "formatSections" must {

    "return nothing for no sections" in {
      SectionFormatter.formatSections(Nil) mustBe Nil
    }

    "turn each answer row into a summary list row" in {

      val rows = SectionFormatter.formatSections(Seq(AnswerSection(Some("heading"), Seq(row, row))))

      rows.size mustBe 2

      rows.head.key.content.asHtml.toString      mustBe messages("site.yes")
      rows.head.value.content.asHtml.toString    mustBe "Yes"
      rows.head.actions.value.items.head.href    mustBe "/change-me"
      rows.head.actions.value.items.head.classes mustBe "change-link-0"
      rows(1).actions.value.items.head.classes   mustBe "change-link-1"
    }

    "reject a repeater section, which the service does not use" in {
      a[NotImplementedError] mustBe thrownBy {
        SectionFormatter.formatSections(Seq(RepeaterAnswerSection("heading", row, Nil, "", "")))
      }
    }
  }

}
