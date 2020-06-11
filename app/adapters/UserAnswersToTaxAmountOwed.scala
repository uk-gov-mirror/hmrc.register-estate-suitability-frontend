/*
 * Copyright 2020 HM Revenue & Customs
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

package adapters

import models.UserAnswers
import pages.{MoreThanHalfMillPage, MoreThanQuarterMillPage, MoreThanTenThousandPage, MoreThanTwoHalfMillPage}

class UserAnswersToTaxAmountOwed {

  def convert(userAnswers: UserAnswers): Option[String] = {
    val answers = (userAnswers.get(MoreThanHalfMillPage),
                   userAnswers.get(MoreThanQuarterMillPage),
                   userAnswers.get(MoreThanTenThousandPage),
                   userAnswers.get(MoreThanTwoHalfMillPage))

    answers match {

      case (None, Some(false), Some(true), None) => Some("01")
      case (None, Some(true), None, None) => Some("02")
      case (Some(true), None, None, None) => Some("03")
      case (None, Some(false), Some(false), Some(true)) => Some("04")
      case _ => None
    }
  }

}
