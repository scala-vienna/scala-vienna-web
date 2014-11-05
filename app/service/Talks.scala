package service

import java.io.File

import scala.Array.canBuildFrom
import scala.io.Codec
import scala.io.Source
import scala.util.Try

import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.pegdown.PegDownProcessor

import com.typesafe.config.ConfigFactory

import play.api.Logger.debug
import play.api.Logger.warn
import play.api.Play
import play.api.Play.current
import play.api.cache.Cache
import play.twirl.api.Html

import scala.concurrent.duration._

case class Talk(
    // basic
    date: LocalDate,
    slug: String,
    title: String,
    speaker: String,
    // social
    meetupEventId: Option[String],
    meetupMemberId: Option[String],
    twitter: Option[String],
    homepage: Option[String],
    // presentation
    code: Option[String],
    slides: Option[String],
    video: Option[String],
    // content
    teaser: Html,
    content: Html,
    tags: Set[String]) {
  def pictureUrl = video.map(v => v.substring(v.indexOf("=") + 1))
}

/** The cached talks */
case class Talks(talks: Vector[Talk], speakers: Seq[String], tags: Seq[String], dates: Seq[String])

/** All infos for a single talk list page*/
case class TalksUI(talks: Vector[Talk], page: Int, pages: Int, speakers: Seq[String], tags: Seq[String], dates: Seq[String])

/** All infos for a single talk page */
case class TalkUI(talk: Option[Talk], speakers: Seq[String], tags: Seq[String], dates: Seq[String])

object Talks {
  val dateUIFormat = DateTimeFormat.forPattern("YYYY-MM-dd")

  private val talksDirectory = "conf/talks"
  private val pageSize = Play.configuration.getInt("talks.talksPerPage").getOrElse(5)
  private val filenameRegex = """(20\d{6})_([^.]+)\.(md|markdown)""".r
  private val dateMDFormat = DateTimeFormat.forPattern("YYYYMMdd")

  // not thread-safe but only used sequentially
  val pegDownProcessor = new PegDownProcessor()

  // Sort talks descending by date and ascending by slug
  private object TalkOrdering extends Ordering[Talk] {
    def compare(o1: Talk, o2: Talk) = {
      val dateComparison = -o1.date.compareTo(o2.date)
      if (dateComparison != 0) dateComparison
      else o1.slug.compareTo(o2.slug)
    }
  }

  private def parseTalk(file: File): Option[Talk] = try {
    debug("Parsing talk " + file.getName())
    def separateHeaderContent = {
      val lines = Source.fromFile(file)(Codec.UTF8).getLines
      // drop header ---
      lines.drop(1)
      val header = lines.takeWhile(!_.startsWith("---")).mkString("\n")
      val properties = ConfigFactory.parseString(header)
      (properties, lines.toSeq)
    }
    val filenameRegex(dateString, slug, _) = file.getName
    val date = dateMDFormat.parseLocalDate(dateString)
    val (properties, contentLines) = separateHeaderContent
    // basic
    val title = properties.getString("title")
    val speaker = properties.getString("speaker")
    // social
    val meetupEventId = Try(properties.getString("meetupEventId")).toOption
    val meetupMemberId = Try(properties.getString("meetupMemberId")).toOption
    val twitter = Try(properties.getString("twitter")).toOption
    val homepage = Try(properties.getString("homepage")).toOption
    // presentation
    val code = Try(properties.getString("code")).toOption
    val slides = Try(properties.getString("slides")).toOption
    val video = Try(properties.getString("video")).toOption
    // content
    val content = markdown(contentLines.mkString("\n"))
    val teaser = markdown(contentLines.take(3).mkString("\n"))
    val tags = Try(properties.getString("tags").split(",").toSet).toOption.getOrElse(Set.empty)

    Some(Talk(date = date, slug = slug, title = title, speaker = speaker,
      meetupEventId = meetupEventId, meetupMemberId = meetupMemberId, twitter = twitter, homepage = homepage,
      code = code, slides = slides, video = video,
      teaser = teaser, content = content, tags = tags))
  } catch {
    case e: Exception =>
      warn(s"Couldn't parse ${file}", e)
      None
  }

  def fetchPaginated(page: Int = 1, tag: Option[String] = None, speaker: Option[String] = None, date: Option[LocalDate] = None): TalksUI = {
    val all = fetchList(tag, speaker, date)
    val start = (page - 1) * pageSize
    val paginatedTalks = all.talks.slice(start, start + pageSize)
    val pages = Math.ceil(all.talks.size / pageSize.toDouble).toInt
    TalksUI(talks = paginatedTalks, page = page, pages = pages, speakers = all.speakers, tags = all.tags, dates = all.dates)
  }

  def fetchVideos() = {
    val all = fetchList(None, None, None)
    all.talks.filter(_.video.isDefined)
  }

  private def fetchList(tag: Option[String] = None, speaker: Option[String] = None, date: Option[LocalDate] = None): Talks = {
    val talks = Cache.getAs[Talks]("talks") match {
      // all talks
      case Some(talks) => talks
      case None => {
        val files = try {
          debug("Application path is: " + Play.application.path.getAbsolutePath)
          val dir = Play.getFile(talksDirectory)
          if (dir != null && dir.exists() && dir.isDirectory()) {
            dir.listFiles()
          } else {
            warn("Couldn't find valid Play talks directory")
            Array.empty[File]
          }
        } catch {
          case e: Exception =>
            warn("Error while fetching talks list from Play talks directory", e)
            Array.empty[File]
        }
        // flatMapping removes all invalid talks
        val data = files.flatMap(parseTalk)
        val speakers = data.map(_.speaker).toSeq.distinct.sorted
        val tags = data.map(_.tags).flatten.toSeq.distinct.sorted
        val dates = data.map(_.date).toSeq.distinct.map(dateUIFormat.print).sorted.reverse
        val dataSorted = data.to[Vector].sorted(TalkOrdering)
        val talks = Talks(dataSorted, speakers, tags, dates)
        // TODO set useful timeout for production
        Cache.set("talks", talks, 30.seconds)
        talks
      }
    }
    // filtered talks
    (tag, speaker, date) match {
      case (Some(tag), _, _) => talks.copy(talks = talks.talks.filter(_.tags.contains(tag)))
      case (_, Some(speaker), _) => talks.copy(talks = talks.talks.filter(_.speaker == speaker))
      case (_, _, Some(date)) => talks.copy(talks = talks.talks.filter(_.date == date))
      case (_, _, _) => talks
    }
  }

  def fetchSingle(date: LocalDate, slug: String): TalkUI = {
    val all = fetchList()
    val talk = all.talks.filter { talk => talk.date == date && talk.slug == slug }.headOption
    TalkUI(talk = talk, speakers = all.speakers, tags = all.tags, dates = all.dates)
  }

  private def markdown(markdown: String): Html = Html(pegDownProcessor.markdownToHtml(markdown))

}