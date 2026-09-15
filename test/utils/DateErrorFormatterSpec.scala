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
import play.api.data.FormError

class DateErrorFormatterSpec extends SpecBase {

  "formatArgs" must {

    "translate and lower case each date part" in {
      DateErrorFormatter.formatArgs(Seq("day", "month", "year")) mustBe
        Seq(messages("date.day"), messages("date.month"), messages("date.year")).map(_.toLowerCase)
    }

    "return nothing for no args" in {
      DateErrorFormatter.formatArgs(Nil) mustBe Nil
    }
  }

  "addErrorClass" must {

    "add the error class when the error names the date part" in {
      DateErrorFormatter.addErrorClass(Some(FormError("value", "error.required", Seq("day"))), "day") mustBe
        "govuk-input--error"
    }

    "add the error class when the error names no parts at all" in {
      DateErrorFormatter.addErrorClass(Some(FormError("value", "error.invalid", Nil)), "day") mustBe
        "govuk-input--error"
    }

    "not add the error class when the error names a different date part" in {
      DateErrorFormatter.addErrorClass(Some(FormError("value", "error.required", Seq("month"))), "day") mustBe ""
    }

    "not add the error class when there is no error" in {
      DateErrorFormatter.addErrorClass(None, "day") mustBe ""
    }
  }

}
