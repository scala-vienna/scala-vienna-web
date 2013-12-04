package service

import java.util.Date
import java.net.{ HttpURLConnection, URL }
import com.sun.syndication.io.{ XmlReader, SyndFeedInput }
import com.sun.syndication.feed.synd.{ SyndCategory, SyndContent, SyndEntry }
import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import play.api.Logger
import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps

case class Post(title: String,
  link: String,
  author: String,
  description: Option[String],
  publishedDate: Option[Date],
  categories: Set[String])

case class Blog(url: String, categories: Set[String])

object Posts {

  val defaultCategories = Set("scala", "akka", "play", "reactive")

  val blogs = Seq(
    Blog("http://rafael.cordones.me/feed/", defaultCategories),
    Blog("http://devsketches.blogspot.com/feeds/posts/default", defaultCategories),
    Blog("http://blog.papauschek.com/feed/", defaultCategories),
    Blog("http://logician.eu/feed/", defaultCategories)
  )

  def readEntries(timeout: Duration = Duration.Zero)(blog: Blog): Future[List[Post]] = {
    val fetchFuture = Future {
      val url = new URL(blog.url)
      val conn: HttpURLConnection = url.openConnection match {
        case conn: HttpURLConnection => conn
      }
      val input = new SyndFeedInput()
      val feed = input.build(new XmlReader(conn))
      feed.getEntries.toList.map({
        case e: SyndEntry => {
          Post(
            title = e.getTitle,
            author = Option(e.getAuthor).getOrElse(e.getAuthors.mkString(", ")),
            link = e.getLink,
            description = Option(Option(e.getDescription).map(_.getValue).getOrElse(e.getContents.map({
              case c: SyndContent => c.getValue
            }).mkString("\n"))),
            publishedDate = Option(Option(e.getPublishedDate).getOrElse(e.getUpdatedDate)),
            categories = e.getCategories.map({
              case c: SyndCategory => c.getName.toLowerCase
            }).toSet
          )
        }
      }).filter(!_.categories.intersect(blog.categories).isEmpty)
    }
    if (timeout > Duration.Zero)
      Future.firstCompletedOf(Seq(fetchFuture, Future[List[Post]] { blocking { Thread.sleep(timeout.toMillis); Nil } }))
    else
      Future.firstCompletedOf(Seq(fetchFuture))
  }

  def blogPosts: Future[List[Post]] = {
    val p = Promise[List[Post]]()
    val futures = blogs.toList.map(readEntries(2 seconds)).map(_.recover { case e => { Logger.warn(s"Couldn't get blog posts: ${e.getMessage}"); Nil } })
    Future.sequence(futures).onSuccess { case blogPosts => p.success(blogPosts.flatten) }
    p.future
  }

}
