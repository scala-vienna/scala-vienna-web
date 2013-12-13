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
import play.api.Play
import play.api.Play.current
import play.api.templates.Html

case class Talk(
  // basic
  date: LocalDate,
  slug: String,
  title: String,
  author: String,
  // social
  meetup: Option[String],
  twitter: Option[String],
  // presentation
  code: Option[String],
  slides: Option[String],
  video: Option[String],
  // content
  teaser: Html,
  content: Html)

object Talks {
  val talksDirectory = "talks"
  private val filenameRegex = """(20\d{6})_([^.]+)\.(md|markdown)""".r
  private val dateFormat = DateTimeFormat.forPattern("YYYYMMdd")

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
    val author = properties.getString("author")
    // social
    val meetup = Try(properties.getString("meetup")).toOption
    val twitter = Try(properties.getString("twitter")).toOption
    // presentation
    val code = Try(properties.getString("code")).toOption
    val slides = Try(properties.getString("slides")).toOption
    val video = Try(properties.getString("video")).toOption
    // content
    val content = markdown(contentLines.mkString("\n"))
    val teaser = markdown(contentLines.take(3).mkString("\n"))
    Talk(date = date, slug = slug, title = title, author = author,
      meetup = meetup, twitter = twitter,
      code = code, slides = slides, video = video,
      teaser = teaser, content = content)
  }

  def fetchAll: Seq[Talk] = {
    val talks = Play.getFile(talksDirectory).listFiles() map (parseTalk)
    talks
  }

  def fetch(date: LocalDate, slug: String): Option[Talk] = {
    fetchAll.find { talk => talk.date == date && talk.slug == slug }
  }

  def markdown(markdown: String): Html = {
    Html(new PegDownProcessor().markdownToHtml(markdown))
  }

}