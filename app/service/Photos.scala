package service

import scala.concurrent.{ Await, Future }
import play.api.libs.json._
import javax.imageio.ImageIO
import java.net.URL
import akka.actor.{ Props, Actor }
import play.api.libs.concurrent.Akka
import scala.concurrent.duration._
import play.api.Play.current
import akka.util.Timeout

object Photos extends MeetupApi[Photo] {

  lazy val imageSizeReader = Akka.system.actorOf(Props[ImageSizeReader])

  def findAll: Future[Seq[Photo]] = findAll(
    entityType = "photos",
    processor = transformJsonResponse
  )

  def findAllByEventAndUser(eventId: String, userId: String): Future[Seq[Photo]] = findAll(
    entityType = "photos",
    processor = transformJsonResponse,
    queryParams = "event_id" -> eventId, "tagged" -> userId
  )

  private def transformJsonResponse(response: JsValue): Seq[Photo] = {
    //Logger.debug("Received JSON: " + response.toString())

    (response \ "results").as[JsArray].value.flatMap { photo =>

      for {
        id <- (photo \ "photo_id").asOpt[Int]
        eventId <- (photo \ "photo_album" \ "event_id").asOpt[String]
        thumbnail <- (photo \ "thumb_link").asOpt[String]
        medium <- (photo \ "photo_link").asOpt[String]
        highres <- (photo \ "highres_link").asOpt[String]
        caption <- (photo \ "caption").asOpt[String].orElse(Some("")) // Most pictures do not have a caption!
      } yield {
        // We want to store the sizes so we can filter only the landscape photos for the front page
        // TODO we should rid ourselves of the blocking here, and adapt the interface to deal with a Future result
        import akka.pattern.ask
        implicit val timeout = Timeout(2.second)
        val eventuallySize = imageSizeReader ? thumbnail
        val (width: Int, height: Int) = try {
          Await.result(eventuallySize, 2.seconds).asInstanceOf[(Int, Int)]
        } catch {
          case t: Throwable =>
            (0, 0)
        }

        Photo(
          id = id.toString(),
          eventId = eventId,
          caption = caption,
          thumbnailUrl = thumbnail,
          // FIXME: there does not seem to be a way to access this bigger thumbnail via the API!
          mediumUrl = medium, //thumbnail.replaceAllLiterally("thumb_", "global_"),
          highresUrl = highres,
          width = width, height = height
        )
      }
    }
  }
}

class ImageSizeReader extends Actor {

  def receive = {
    case thumbnail: String =>
      try {
        val image = ImageIO.read(new URL(thumbnail))
        val height = image.getHeight
        val width = image.getWidth
        sender ! (height, width)
      } catch {
        case t: Throwable =>
          sender ! (0, 0)
      }
  }

}

case class Photo(
  id: String,
  eventId: String,
  caption: String,
  thumbnailUrl: String,
  mediumUrl: String,
  highresUrl: String,
  width: Int, height: Int)
