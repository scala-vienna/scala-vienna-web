package controllers

import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import service.{ Post, Posts }
import play.api.cache.Cache
import scala.concurrent.{ Future, Promise }
import play.api.Play.current
import service.Github
import service.Talk
import service.Talks
import org.joda.time.LocalDate
import scala.util.Try

object Application extends Controller {

  def index = Action.async { implicit request =>
    {
      for {
        photos <- Photos.findAll
        upcoming <- Meetup.retrieveEvents("upcoming")
        past <- Meetup.retrieveEvents("past")
      } yield {
        val randomPhotos = scala.util.Random.shuffle(photos).slice(0, 4).toList
        Ok(views.html.index(upcoming, past.reverse, randomPhotos))
      }
    }
  }

  def photos = Action.async { implicit request =>
    Photos.findAll.map { allPhotos =>
      Ok(views.html.photos(allPhotos))
    }
  }

  def talks(page: Int, tagFilter: Option[String] = None, speakerFilter: Option[String] = None, dateFilter: Option[String] = None) = Action { implicit request =>
    val jodaDate: Option[LocalDate] = dateFilter match {
      case Some(date) => Try { Talks.dateUIFormat.parseLocalDate(date) }.toOption
      case _ => None
    }
    val talks = Talks.fetchPaginated(page, tagFilter, speakerFilter, jodaDate)
    Ok(views.html.talks(talks.talks, talks.page, talks.pages, talks.speakers, talks.tags, talks.dates, tagFilter, speakerFilter, dateFilter))
  }

  def talk(year: Int, month: Int, day: Int, slug: String) = Action {
    val talkInfos = Talks.fetchSingle(new LocalDate(year, month, day), slug)
    Ok(views.html.talk(talkInfos.talks.headOption, talkInfos.speakers, talkInfos.tags, talkInfos.dates))
  }

  def group = Action {
    Ok(views.html.group())
  }

  def blogs = Action.async { implicit request =>
    {
      def tags(posts: List[Post]) = {
        val cats = posts.flatMap(_.categories).groupBy(s => s).map(v => (v._1, v._2.length))
        val max = cats.values.max
        val norm = cats.toList.map(v => (v._1, 1 + v._2 * 4 / max))
        norm.toList
      }
      val p = Promise[SimpleResult]()
      val filterOpt = request.getQueryString("filter")
      def filter(ps: List[Post]): List[Post] = (filterOpt match {
        case None => ps
        case Some(filter) => ps.filter(_.categories.contains(filter))
      }).take(10)
      Cache.getAs[List[Post]]("posts") match {
        case Some(posts) =>
          p.success(Ok(views.html.blogs(filter(posts), tags(posts))))
        case None => {
          Posts.blogPosts.map(result => {
            val posts = result.sortBy(_.publishedDate).reverse
            Cache.set("posts", posts, 300) // put result on 5 minutes in cache
            p.success(Ok(views.html.blogs(filter(posts), tags(posts))))
          })
        }
      }
      p.future
    }
  }

  def about = Action.async {
    Github.getContributors.flatMap(conts => {
      val userFutures = conts.map(cont => Github.getUser(cont.url))
      Future.sequence(userFutures).map(users => Ok(views.html.about(util.Random.shuffle(users))))
    })
  }

}