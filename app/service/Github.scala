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

  val apiKey = Play.configuration.getString("github.apiKey").getOrElse(scala.util.Properties.envOrElse("GITHUB_API_KEY", ""))

  implicit val contributorFormat = Json.format[Contributor]

  def getContributors: Future[List[Contributor]] = {
    WS.url("https://api.github.com/repos/rafacm/scala-vienna-web/contributors")
      .withAuth(apiKey, "x-oauth-basic", Realm.AuthScheme.BASIC)
      .get().map(
        response =>
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

}

