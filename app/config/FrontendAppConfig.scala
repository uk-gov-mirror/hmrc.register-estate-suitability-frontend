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

package config

import com.google.inject.{Inject, Singleton}
import controllers.routes
import play.api.Configuration
import play.api.i18n.Lang
import play.api.mvc.Call
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class FrontendAppConfig @Inject() (
  configuration: Configuration,
  servicesConfig: ServicesConfig
) {

  lazy val authUrl: String           = servicesConfig.baseUrl("auth")
  lazy val loginUrl: String          = configuration.get[String]("urls.login")
  lazy val loginContinueUrl: String  = configuration.get[String]("urls.loginContinue")
  lazy val basGatewayBaseUrl: String = servicesConfig.getString("bas-gateway.host")

  lazy val logoutUrl: String  = configuration.get[String]("urls.logout")
  lazy val logout: String     = s"$basGatewayBaseUrl$logoutUrl"
  lazy val timeOutUrl: String = configuration.get[String]("urls.timeOut")

  lazy val feedbackFrontendUrl: String =
    s"${configuration.get[String]("feedback-frontend.url")}?useServiceNavigation"

  lazy val logoutAudit: Boolean =
    configuration.get[Boolean]("microservice.services.features.auditing.logout")

  lazy val countdownLength: Int = configuration.get[Int]("timeout.countdown")
  lazy val timeoutLength: Int   = configuration.get[Int]("timeout.length")

  lazy val agentDetails: String         = configuration.get[String]("urls.agentDetails")
  lazy val registrationProgress: String = configuration.get[String]("urls.registrationProgress")

  lazy val estatesUrl: String = servicesConfig.baseUrl("estates")

  lazy val cachettlSessionInSeconds: Long = configuration.get[Int]("mongodb.timeToLiveInSeconds")

  def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy")
  )

  lazy val registerEstatesUrl: String = servicesConfig.baseUrl("register-an-estate")

  def routeToSwitchLanguage: String => Call =
    (lang: String) => routes.LanguageSwitchController.switchToLanguage(lang)

}
