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

  val launchTime = new DateTime(2013, 12, 17, 18, 30, DateTimeZone.forID("Europe/Vienna"))

  def apply(next: (RequestHeader) => Future[SimpleResult])(rh: RequestHeader): Future[SimpleResult] = {

    if (Play.isProd) {

      val now = new DateTime()
      now match {
        case t if t.isAfter(launchTime) && t.isBefore(launchTime.plusMinutes(1)) =>
          Future.successful {
            // TODO Rafa
            Ok(views.html.index(Seq.empty, Seq.empty, Seq.empty))
          }
        case t if t.isAfter(launchTime.plusMinutes(1)) =>
          next(rh)
        case t if t.isBefore(launchTime) =>
          Future.successful {
            Ok(views.html.countdown())
          }
      }

    } else {
      next(rh)
    }
  }
}