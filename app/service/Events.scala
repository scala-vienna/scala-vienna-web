package service

import play.api.libs.concurrent.Promise
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future
import play.api.libs.json._
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTime
import scala.concurrent.duration._

/**
 * Fetch events and more from Meetup
 *
 * @author Manuel Bernhardt <manuel@bernhardt.io>
 */
object Events extends MeetupApi[MeetupEvent] {

  def findAll(status: String): Future[Seq[MeetupEvent]] = {
    val eventuallyAllEvents = findAll(
      entityType = "events",
      processor = transformEvents,
      queryParams = "status" -> status, "text_format" -> "plain"
    )
    val eventuallyTimeout = Promise.timeout("Meetup timeout", 3.seconds)
    Future.firstCompletedOf(Seq(eventuallyAllEvents, eventuallyTimeout)).map {
      case events: Seq[MeetupEvent] => events
      case timeout: String => Seq.empty
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
          description = description.replaceAll("\n+", "\n") // remove multiple line separators
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