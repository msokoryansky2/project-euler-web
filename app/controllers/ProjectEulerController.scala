package controllers

import javax.inject._

import play.api.libs.json.Json
import play.api.mvc._
import services.EulerProblemService
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * This controller creates an `Action` to handle HTTP requests for Project Euler solutions
  */
@Singleton
class ProjectEulerController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  def index(num: Int) = Action.async { implicit request: Request[AnyContent] =>
    EulerProblemService.answer(num).map(result => Ok(Json.toJson(Map(num -> result))))
  }
}
