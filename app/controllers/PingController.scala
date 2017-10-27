package controllers

import javax.inject._

import play.api.mvc._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page which lists Project Euler problems with available solutions
  */
@Singleton
class PingController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  def index() = Action.async { implicit request: Request[AnyContent] =>
    Future {
      Ok(views.html.ping())
    }
  }
}
