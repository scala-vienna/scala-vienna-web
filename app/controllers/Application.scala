package controllers

import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import service.{ Post, Posts }
import play.api.cache.Cache
import scala.concurrent.{ Future, Promise }
import play.api.Play.current

object Application extends Controller {

  def index = Action.async { implicit request =>
    {
      for {
        upcoming <- Meetup.retrieveEvents("upcoming")
        past <- Meetup.retrieveEvents("past")
      } yield Ok(views.html.index(upcoming, past.reverse))
    }
  }

  def photos = Action.async { implicit request =>
    Photos.findAll.map { allPhotos =>
      Ok(views.html.photos(allPhotos))
    }
  }

  def talks = Action {
    Ok(views.html.talks())
  }

  def group = Action {
    Ok(views.html.group())
  }

  def blogs = Action.async { implicit request =>
    {
      val p = Promise[SimpleResult]()
      Cache.getAs[List[Post]]("posts") match {
        case Some(posts) =>
          p.success(Ok(views.html.blogs(posts)))
        case None => {
          Posts.blogPosts.map(result => {
            val posts = result.sortBy(_.publishedDate).reverse
            Cache.set("posts", posts, 300) // put result on 5 minutes in cache
            p.success(Ok(views.html.blogs(posts)))
          })
        }
      }
      p.future
    }
  }

  def about = Action {
    Ok(views.html.about())
  }

}