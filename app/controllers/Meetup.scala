package controllers

import scala.concurrent.Future
import play.api.libs.ws._
import play.api.libs.json._
import play.api.{ Logger, Play }
import play.api.Play.current
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTime
import play.api.libs.concurrent.Execution.Implicits._

/**
 * Fetch events and more from Meetup
 *
 * @author Manuel Bernhardt <manuel@bernhardt.io>
 */
object Meetup {

  val envApiKey = scala.util.Properties.envOrElse("MEETUP_API_KEY", "")
  val apiKey = Play.configuration.getString("meetup.apiKey").getOrElse(envApiKey)

  def upcomingEvents: Future[Seq[MeetupEvent]] = {
    WS.url("https://api.meetup.com/2/events")
      .withQueryString("group_id" -> "5700242", "key" -> apiKey)
      .get()
      .map { result =>
        if (result.status == 200) {
          transformEvents(result.json)
        } else {
          Logger.warn("Could not retrieve events form meetup. Did you configure the apiKey in the application configuration or the MEETUP_API_KEY environment variable?")
          Seq.empty
        }
      }
  }

  private def transformEvents(events: JsValue): Seq[MeetupEvent] = {

    val dateTimeFormatter = DateTimeFormat.forPattern("dd.MM.YYYY kk:mm")

    (events \ "results").as[JsArray].value.flatMap { event =>

      for {
        id <- (event \ "id").asOpt[String]
        name <- (event \ "name").asOpt[String]
        time <- (event \ "time").asOpt[Long]
        utc_offset <- (event \ "utc_offset").asOpt[Long]
        url <- (event \ "event_url").asOpt[String]
        description <- (event \ "description").asOpt[String]
        venueName <- (event \ "venue" \ "name").asOpt[String]
        address <- (event \ "venue" \ "address_1").asOpt[String]
        city <- (event \ "venue" \ "city").asOpt[String]
        lat <- (event \ "venue" \ "lat").asOpt[Double]
        lon <- (event \ "venue" \ "lon").asOpt[Double]
      } yield {
        MeetupEvent(
          id = id,
          name = name,
          venue = MeetupVenue(
            name = venueName,
            address = address,
            city = city,
            lat = 0.0,
            lon = 0.0
          ),
          displayTime = dateTimeFormatter.print(new DateTime(time + utc_offset)),
          url = url,
          description = description
        )
      }
    }
  }

}

case class MeetupEvent(
  id: String,
  name: String,
  url: String,
  description: String,
  venue: MeetupVenue,
  displayTime: String)

case class MeetupVenue(
  name: String,
  address: String,
  city: String,
  lat: Double,
  lon: Double)