import org.joda.time.{ DateTimeZone, DateTime }
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import play.api.mvc.SimpleResult
import scala.concurrent.Future
import play.api.Play.current

object Global extends WithFilters(new CountdownFilter) {

  override def onError(request: RequestHeader, ex: Throwable) = {
    Future.successful(InternalServerError(
      views.html.serverError(ex)
    ))
  }

}

/**
 * TODO remove after launch
 */
class CountdownFilter extends Filter with Results {

  val launchTime = new DateTime(2013, 12, 17, 19, 1, DateTimeZone.forID("Europe/Vienna"))

  def apply(next: (RequestHeader) => Future[SimpleResult])(rh: RequestHeader): Future[SimpleResult] = {

    // To bypass this logic just access the /enable-dev-access URL to create the cookie!
    // Acccess /disable-dev-access to remove the cookie
    if (rh.path.endsWith("/enable-dev-access") || rh.path.endsWith("/disable-dev-access")) {
      next(rh)
    } else if (rh.cookies.exists(cookie => cookie.name == "DEV_ACCESS")) {
      next(rh)
      // We need to let the CSS, JS and IMG assets of the countdown page be rendered!
    } else if (rh.path.endsWith(".css") || rh.path.endsWith(".js") || rh.path.endsWith(".png") || rh.path.endsWith(".jpg")) {
      next(rh)
    } else {
      val now = new DateTime()
      now match {
        case t if t.isAfter(launchTime) =>
          next(rh)
        case t if t.isBefore(launchTime) =>
          Future.successful {
            Ok(views.html.countdown())
          }
      }
    }
  }
}