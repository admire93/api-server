package controllers

import play.api._
import play.api.mvc._
import play.api.data.Forms._
import play.api.data._

import models._

import play.api.Play.current
import play.api.libs._
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import java.util.concurrent._
import scala.concurrent.stm._
import play.api.cache._
import play.api.libs.json._
import play.api.libs.json.Json._

object Application extends Controller {
  
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def NotFoundJson(code: Int, message: String) = 
    NotFound(Json.obj(
      "status" -> Json.obj(
        "code" -> code,
        "message" -> message
      )
    ))
}