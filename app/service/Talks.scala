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

import play.api.Logger._
import play.api.Play
import play.api.Play.current
import play.api.cache.Cache
import play.api.templates.Html

case class Talk(
  // basic
  date: LocalDate,
  slug: String,
  title: String,
  speaker: String,
  // social
  meetup: Option[String],
  twitter: Option[String],
  homepage: Option[String],
  // presentation
  code: Option[String],
  slides: Option[String],
  video: Option[String],
  // content
  teaser: Html,
  content: Html,
  tags: Set[String])

case class Talks(talks: Seq[Talk], speakers: Seq[String], tags: Seq[String])

object Talks {
  val talksDirectory = "public/talks"
  private val filenameRegex = """(20\d{6})_([^.]+)\.(md|markdown)""".r
  private val dateFormat = DateTimeFormat.forPattern("YYYYMMdd")

  // Sort talks descending by date and ascending by slug
  private object TalkOrdering extends Ordering[Talk] {
    def compare(o1: Talk, o2: Talk) = {
      val dateComparison = -o1.date.compareTo(o2.date)
      if (dateComparison != 0) dateComparison
      else o1.slug.compareTo(o2.slug)
    }
  }

  private def parseTalk(file: File): Talk = {
    debug("Parsing talk " + file)
    def separateHeaderContent = {
      val lines = Source.fromFile(file)(Codec.UTF8).getLines
      // drop header ---
      lines.drop(1)
      val header = lines.takeWhile(!_.startsWith("---")).mkString("\n")
      val properties = ConfigFactory.parseString(header)
      (properties, lines.toSeq)
    }
    val filenameRegex(dateString, slug, _) = file.getName
    val date = dateFormat.parseLocalDate(dateString)
    val (properties, contentLines) = separateHeaderContent
    // basic
    val title = properties.getString("title")
    val speaker = properties.getString("speaker")
    // social
    val meetup = Try(properties.getString("meetup")).toOption
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

    Talk(date = date, slug = slug, title = title, speaker = speaker,
      meetup = meetup, twitter = twitter, homepage = homepage,
      code = code, slides = slides, video = video,
      teaser = teaser, content = content, tags = tags)
  }

  def fetchList(tag: Option[String] = None, speaker: Option[String] = None): Talks = {
    val talks = Cache.getAs[Talks]("talks") match {
      case Some(talks) => talks
      case None => {
        val files = try {
          debug("Application path is: " + Play.application.path.getName)
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
        val data = files.map(parseTalk)
        val speakers = data.map(_.speaker).distinct
        val tags = data.map(_.tags).flatten.distinct
        val dataSorted = data.sorted(TalkOrdering)
        val talks = Talks(dataSorted, speakers, tags)
        // TODO set useful timeout for production
        // TODO: remove from Cache while debugging Heroku issue: Cache.set("talks", talks, 30)
        talks
      }
    }
    (tag, speaker) match {
      case (Some(tag), _) => talks.copy(talks = talks.talks.filter(_.tags.contains(tag)))
      case (_, Some(speaker)) => talks.copy(talks = talks.talks.filter(_.speaker == speaker))
      case (None, None) => talks
    }
  }

  def fetchSingle(date: LocalDate, slug: String): Talks = {
    val all = fetchList()
    all.copy(talks = all.talks.filter { talk => talk.date == date && talk.slug == slug })
  }

  def markdown(markdown: String): Html = {
    Html(new PegDownProcessor().markdownToHtml(markdown))
  }

}