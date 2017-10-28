package controllers

import javax.inject._

import play.api.Configuration
import play.api.mvc._
import services.EulerProblemService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page which lists Project Euler problems with available solutions
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents,
                               configuration: Configuration) extends AbstractController(cc) {
  val logger = play.api.Logger(getClass)
  val googleApiKey: String = configuration.getOptional[String]("google.api.key").getOrElse("N/A")

  def index() = Action.async { implicit request: Request[AnyContent] =>
    logger.info(s"Web request for home page")
    Future {
      EulerProblemService.availableProblems
    } map (problemList => Ok(views.html.main(views.html.right(problemList))(googleApiKey)))
  }
}
