package controllers

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import service.Posts

object Application extends Controller {

  def index = Action.async { implicit request =>

    Meetup.upcomingEvents.map { upcoming =>
      Ok(views.html.index(upcoming))
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

  def blogs = Action {
    Ok(views.html.blogs(Posts.blogPosts))
  }

  def about = Action {
    Ok(views.html.about())
  }

}