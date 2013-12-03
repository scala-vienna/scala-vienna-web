package service

import java.util.Date
import java.net.{ HttpURLConnection, URL }
import com.sun.syndication.io.{ XmlReader, SyndFeedInput }
import com.sun.syndication.feed.synd.{ SyndContent, SyndEntry }
import scala.collection.JavaConversions._
import scala.concurrent.{ Promise, ExecutionContext, Future }
import ExecutionContext.Implicits.global
import play.api.Logger

case class Post(title: String,
  link: String,
  author: String,
  description: Option[String],
  publishedDate: Option[Date])

object Posts {

  val blogs = Seq(
    "http://rafael.cordones.me/feed/",
    "http://devsketches.blogspot.com/feeds/posts/default",
    "http://blog.papauschek.com/feed/"
  )

  def readEntries(feedUrl: String): Future[List[Post]] = Future {
    val url = new URL(feedUrl)
    val conn: HttpURLConnection = url.openConnection match {
      case conn: HttpURLConnection => conn
    }
    val input = new SyndFeedInput()
    val feed = input.build(new XmlReader(conn))
    feed.getEntries.toList.map {
      case e: SyndEntry => {
        Post(
          title = e.getTitle,
          author = Option(e.getAuthor).getOrElse(e.getAuthors.mkString(", ")),
          link = e.getLink,
          description = Option(Option(e.getDescription).map(_.getValue).getOrElse(e.getContents.map({
            case c: SyndContent => c.getValue
          }).mkString("\n"))),
          publishedDate = Option(Option(e.getPublishedDate).getOrElse(e.getUpdatedDate))
        )
      }
    }
  }

  def blogPosts: Future[List[Post]] = {
    val p = Promise[List[Post]]()
    val futures = blogs.toList.map(readEntries).map(_.recover { case e => { Logger.warn(s"Couldn't get blog posts: ${e.getMessage}"); Nil } })
    Future.sequence(futures).onSuccess { case blogPosts => p.success(blogPosts.flatten) }
    p.future
  }

}
