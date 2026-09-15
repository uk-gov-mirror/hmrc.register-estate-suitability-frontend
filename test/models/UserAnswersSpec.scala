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

package models

import base.SpecBase
import pages.{DateOfDeathBeforePage, MoreThanHalfMillPage, MoreThanQuarterMillPage}
import play.api.libs.json.{JsResultException, Json}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

class UserAnswersSpec extends SpecBase {

  private val instant = Instant.ofEpochMilli(1757808000000L)

  "get" must {

    "return the stored answer" in {
      emptyUserAnswers.set(MoreThanHalfMillPage, true).success.value.get(MoreThanHalfMillPage).value mustBe true
    }

    "return None when the page is unanswered" in {
      emptyUserAnswers.get(MoreThanHalfMillPage) mustBe None
    }

    "return None when the stored value is the wrong type" in {
      val answers = UserAnswers(userAnswersId, Json.obj(MoreThanHalfMillPage.toString -> "not a boolean"))
      answers.get(MoreThanHalfMillPage) mustBe None
    }
  }

  "set" must {

    "store the answer, running the page cleanup" in {
      val answers = emptyUserAnswers
        .set(MoreThanQuarterMillPage, true)
        .success
        .value
        .set(DateOfDeathBeforePage, false)
        .success
        .value

      answers.get(DateOfDeathBeforePage).value mustBe false

      answers.get(MoreThanQuarterMillPage) mustBe None
    }

    "fail when the path cannot be written to" in {
      val answers = UserAnswers(userAnswersId, Json.obj(DateOfDeathBeforePage.toString -> Json.arr()))
      answers.set(DateOfDeathBeforePage, true).success.value.get(DateOfDeathBeforePage).value mustBe true
    }
  }

  "remove" must {

    "clear a stored answer" in {
      val answers = emptyUserAnswers.set(MoreThanHalfMillPage, true).success.value
      answers.remove(MoreThanHalfMillPage).success.value.get(MoreThanHalfMillPage) mustBe None
    }

    "be a no-op when the page was never answered" in {
      emptyUserAnswers.remove(MoreThanHalfMillPage).success.value.get(MoreThanHalfMillPage) mustBe None
    }
  }

  "format" must {

    "round trip through the mongo representation" in {

      val answers = UserAnswers(userAnswersId, Json.obj("a" -> "b"), instant)

      val json = Json.toJson(answers)(UserAnswers.writes)

      json mustBe Json.obj(
        "_id"         -> userAnswersId,
        "data"        -> Json.obj("a" -> "b"),
        "lastUpdated" -> Json.toJson(instant)(MongoJavatimeFormats.instantWrites)
      )

      json.as(UserAnswers.reads) mustBe answers
    }
  }

}
