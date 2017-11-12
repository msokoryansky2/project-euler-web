package controllers

import javax.inject._

import akka.actor.ActorRef
import models.UserInfo
import play.api.mvc._
import services.EulerProblemService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Configuration

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page which lists Project Euler problems with available solutions
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents,
                               configuration: Configuration,
                               @Named("user-info-master-actor") userInfoMaster: ActorRef) extends AbstractController(cc) {
  val logger = play.api.Logger(getClass)
  val googleApiScriptUrl: String = configuration.getOptional[String]("google.api.script.url").getOrElse("N/A")

  def index() = Action.async { implicit request: Request[AnyContent] =>
    logger.info(s"Web request for home page")
    val userInfo: UserInfo = UserInfo(configuration, request, userInfoMaster)
    Future {
      EulerProblemService.availableProblems
    } map (problemList => Ok(views.html.main(views.html.right(problemList))(googleApiScriptUrl)).withSession(userInfo.toMap.toSeq: _*))
  }
}
