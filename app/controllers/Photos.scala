package controllers

import scala.concurrent.Future
import play.api.libs.ws._
import play.api.libs.json._
import play.api.{ Logger, Play }
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._

object Photos {

  val envApiKey = scala.util.Properties.envOrElse("MEETUP_API_KEY", "")
  val apiKey = Play.configuration.getString("meetup.apiKey").getOrElse(envApiKey)

  def findAll: Future[Seq[Photo]] = {
    WS.url("http://api.meetup.com/2/photos")
      .withQueryString("group_id" -> "5700242", "key" -> apiKey)
      .get()
      .map { result =>
        if (result.status == 200) {
          val photos = transformJsonResponse(result.json)
          Logger.info("Parsed " + photos.size + " photos")
          photos
        } else {
          Logger.warn("Could not retrieve photos form meetup. Did you configure the apiKey in the application configuration or the MEETUP_API_KEY environment variable?")
          Seq.empty
        }
      }
  }

  private def transformJsonResponse(response: JsValue): Seq[Photo] = {
    Logger.info("Received JSON: " + response.toString())

    (response \ "results").as[JsArray].value.flatMap { photo =>

      for {
        id <- (photo \ "photo_id").asOpt[Int]
        eventId <- (photo \ "photo_album" \ "event_id").asOpt[String]
        thumbnail <- (photo \ "thumb_link").asOpt[String]
        medium <- (photo \ "photo_link").asOpt[String]
        highres <- (photo \ "highres_link").asOpt[String]
        caption <- (photo \ "caption").asOpt[String].orElse(Some("")) // Most pictures do not have a caption!
      } yield {
        Photo(
          id = id.toString(),
          eventId = eventId,
          caption = caption,
          thumbnailUrl = thumbnail,
          // FIXME: there does not seem to be a way to access this bigger thumbnail via the API!
          mediumUrl = thumbnail.replaceAllLiterally("thumb_", "global_"),
          highresUrl = highres
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
  highresUrl: String)
