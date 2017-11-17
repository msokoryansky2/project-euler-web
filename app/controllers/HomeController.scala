package controllers

import javax.inject._

import akka.actor.ActorRef
import models.UserInfo
import msg.MsgIpResolution
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
                               @Named("user-info-master-actor") userInfoMaster: ActorRef,
                               @Named("client-broadcaster-actor") clientBroadcaster: ActorRef) extends AbstractController(cc) {
  val logger = play.api.Logger(getClass)
  val googleApiScriptUrl: String = configuration.getOptional[String]("google.api.script.url").getOrElse("N/A")

  def index() = Action.async { implicit request: Request[AnyContent] =>
    logger.info(s"Web request for home page")
    val userInfo: UserInfo = UserInfo(configuration, request, userInfoMaster)
    // If we already have a resolved userInfo, then now is the time to broadcast this new user login.
    // Otherwise it'll eventually be broadcast once the IP is resolved.
    if (userInfo.isResolved) clientBroadcaster ! MsgIpResolution(userInfo)
    Future {
      EulerProblemService.availableProblems
    } map (problemList => Ok(views.html.main(userInfo, views.html.right(problemList))(googleApiScriptUrl))
      .withSession(userInfo.toMap.toSeq: _*))
  }
}
