package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def talks = Action {
    Ok(views.html.talks())
  }

  def group = Action {
    Ok(views.html.group())
  }

  def about = Action {
    Ok(views.html.about())
  }

}