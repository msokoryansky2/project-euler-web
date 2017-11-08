package controllers

import javax.inject._

import models.UserInfo
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
  val logger = play.api.Logger(getClass)

  def index() = Action.async { implicit request: Request[AnyContent] =>
    logger.info(s"Web request for home page")
    val userInfo = UserInfo(request)
    Future {
      EulerProblemService.availableProblems
    } map (problemList => Ok(views.html.main(views.html.right(problemList))).withSession(userInfo.toMap.toSeq: _*))
  }
}
