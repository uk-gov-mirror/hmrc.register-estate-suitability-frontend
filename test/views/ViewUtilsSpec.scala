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

package views

import base.SpecBase
import play.api.data.{Form, FormError}
import play.api.data.Forms.{single, text}
import viewmodels.RadioOption

class ViewUtilsSpec extends SpecBase {

  private val form: Form[String] = Form(single("value" -> text))

  "errorPrefix" must {

    "be empty for a form without errors" in {
      ViewUtils.errorPrefix(form) mustBe ""
    }

    "be present for a form with a field error" in {
      ViewUtils.errorPrefix(form.withError("value", "error.required")) mustBe
        s"${messages("error.browser.title.prefix")} "
    }

    "be present for a form with a global error" in {
      ViewUtils.errorPrefix(form.withGlobalError("error.required")) mustBe
        s"${messages("error.browser.title.prefix")} "
    }
  }

  "breadcrumbTitle" must {

    "append the service name and GOV.UK" in {
      ViewUtils.breadcrumbTitle("a page") mustBe s"a page - ${messages("service.name")} - GOV.UK"
    }
  }

  "isDateError" must {

    "be true when the key mentions a date" in {
      ViewUtils.isDateError("dateOfDeath") mustBe true
    }

    "be true when the key mentions when" in {
      ViewUtils.isDateError("whenDidItHappen") mustBe true
    }

    "be false otherwise" in {
      ViewUtils.isDateError("value") mustBe false
    }
  }

  "errorHref" must {

    "point at the named date part when the error args name one" in {
      ViewUtils.errorHref(FormError("value", "error.required", Seq("month"))) mustBe "value.month"
    }

    "point at the yes radio for a yes/no question" in {
      ViewUtils.errorHref(FormError("value", "error.required"), isYesNo = true) mustBe "value-yes"
    }

    "point at the first radio option when options are supplied" in {
      ViewUtils.errorHref(
        FormError("value", "error.required"),
        radioOptions = Seq(RadioOption("prefix", "first"), RadioOption("prefix", "second"))
      ) mustBe RadioOption("prefix", "first").id
    }

    "point at the day field when the key is a date" in {
      ViewUtils.errorHref(FormError("dateOfDeath", "error.required")) mustBe "dateOfDeath.day"
    }

    "point at the day field when the message is a date error" in {
      ViewUtils.errorHref(FormError("value", "error.date.required")) mustBe "value.day"
    }

    "point at the field itself when the message is a yes/no date error" in {
      ViewUtils.errorHref(FormError("value", "error.dateYesNo.required")) mustBe "value"
    }

    "point at the field itself otherwise" in {
      ViewUtils.errorHref(FormError("value", "error.required")) mustBe "value"
    }
  }

}
