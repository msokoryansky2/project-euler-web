package controllers

import javax.inject._

import play.api.mvc._
import services.EulerProblemService
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page which lists Project Euler problems with available solutions
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  def index() = Action.async { implicit request: Request[AnyContent] =>
    Future {
      EulerProblemService.availableProblems
    } map (problemList => Ok(views.html.index(problemList)))
  }
}
