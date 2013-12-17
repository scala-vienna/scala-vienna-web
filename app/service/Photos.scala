package service

import scala.concurrent.Future
import play.api.libs.json._
import play.api.Logger
import javax.imageio.ImageIO
import java.net.URL

object Photos extends MeetupApi[Photo] {

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
        /*
         * We want to store the sizes so we can filter only the landscape photos for the front page
         */
        val image = ImageIO.read(new URL(thumbnail));
        val height = image.getHeight
        val width = image.getWidth

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

case class Photo(
  id: String,
  eventId: String,
  caption: String,
  thumbnailUrl: String,
  mediumUrl: String,
  highresUrl: String,
  width: Int, height: Int)
