/*
* Copyright 2013 Vienna Scala User Group
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
**/

package service

import play.api.libs.ws.WS
import play.Logger
import play.api.libs.json.Json
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import scala.concurrent.Future
import com.ning.http.client.Realm
import play.api.Play
import play.api.Play.current
import java.util.Date

object Github {

  case class Contributor(
    login: String,
    id: Long,
    avatar_url: String,
    gravatar_id: String,
    url: String,
    html_url: String,
    followers_url: String,
    following_url: String,
    gists_url: String,
    starred_url: String,
    subscriptions_url: String,
    organizations_url: String,
    repos_url: String,
    events_url: String,
    received_events_url: String,
    `type`: String,
    site_admin: Boolean,
    contributions: Long)

  case class User(
    login: String,
    id: Long,
    //avatar_url: String,
    gravatar_id: String,
    url: String,
    html_url: String,
    followers_url: String,
    following_url: String,
    //    gists_url: String,
    //    starred_url: String,
    //    subscriptions_url: String,
    //    organizations_url: String,
    //    repos_url: String,
    //    events_url: String,
    received_events_url: String,
    `type`: String,
    site_admin: Boolean,
    name: String,
    company: Option[String],
    blog: String,
    location: Option[String],
    email: Option[String],
    hireable: Boolean,
    bio: Option[String],
    //public_repos: Int,
    //public_gists: Int,
    followers: Int,
    following: Int,
    created_at: Option[Date],
    updated_at: Option[Date])

  val apiKey = Play.configuration.getString("github.apiKey").getOrElse(scala.util.Properties.envOrElse("GITHUB_API_KEY", ""))

  implicit val contributorFormat = Json.format[Contributor]
  implicit val userFormat = Json.format[User]

  def getContributors: Future[List[Contributor]] = {
    WS.url("https://api.github.com/repos/rafacm/scala-vienna-web/contributors")
      .withAuth(apiKey, "x-oauth-basic", Realm.AuthScheme.BASIC)
      .get().map(response =>
        if (response.status == 200) {
          //println(response.json.toString())
          response.json.validate[List[Contributor]].map({
            case list => list
          }).recoverTotal(e => Nil)
        } else {
          Logger.warn(response.statusText)
          Nil
        }

      )
  }

  def getUser(url: String): Future[Option[User]] = {
    WS.url(url)
      .withAuth(apiKey, "x-oauth-basic", Realm.AuthScheme.BASIC)
      .get().map(
        response =>
          if (response.status == 200) {
            //println(response.json)
            response.json.validate[User].map({
              case user => Some(user)
            }).recoverTotal(e => {
              Logger.error("Error trying to fetch Github user", e)
              None
            })
          } else None
      )
  }

}

