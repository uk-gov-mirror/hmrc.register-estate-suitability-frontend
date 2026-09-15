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

import org.jsoup.nodes.Document
import views.html.DoNotNeedToRegisterView

import scala.jdk.CollectionConverters._

class MainTemplateSpec extends ViewSpecBase {

  private lazy val doc: Document = {
    val application = applicationBuilder().build()
    val view        = application.injector.instanceOf[DoNotNeedToRegisterView]
    val rendered    = asDocument(view.apply()(fakeRequest, messages))
    application.stop()
    rendered
  }

  private def hrefContaining(fragment: String): String =
    doc
      .select("a[href]")
      .asScala
      .map(_.attr("href"))
      .find(_.contains(fragment))
      .getOrElse(fail(s"no link containing '$fragment' was rendered on the page"))

  private val sharedPlatUiPages = Seq(
    "/accessibility-statement/estates",
    "/contact/report-technical-problem",
    "/help/cookies",
    "/help/privacy",
    "/help/terms-and-conditions"
  )

  "MainTemplate" must {

    "render the service navigation component" in {
      assertRenderedByCssSelector(doc, ".govuk-service-navigation")

      doc.getElementsByClass("govuk-service-navigation__service-name").text().trim mustBe messages("service.name")
    }

    "render the language toggle inside the service navigation" in
      assertRenderedByCssSelector(doc, ".govuk-service-navigation .hmrc-service-navigation-language-select")

    sharedPlatUiPages.foreach { page =>
      s"link to $page with the useServiceNavigation parameter" in {
        hrefContaining(page) must include("useServiceNavigation")
      }
    }

    "link the service name back to the start of the journey" in {
      doc
        .select(".govuk-service-navigation__service-name a[href]")
        .asScala
        .map(_.attr("href"))
        .headOption
        .value mustBe frontendAppConfig.loginContinueUrl
    }

    "offer a sign out link" in {
      hrefContaining(controllers.routes.LogoutController.logout().url) mustBe
        controllers.routes.LogoutController.logout().url
    }
  }

}
