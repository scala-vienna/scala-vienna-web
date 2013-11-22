package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index.render())
  }

  def talks = Action {
    Ok(views.html.talks.render())
  }

  def group = Action {
    Ok(views.html.group.render())
  }

  def about = Action {
    Ok(views.html.about.render())
  }

}