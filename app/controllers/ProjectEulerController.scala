package controllers

import javax.inject._

import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration._
import akka.pattern.ask
import msg.{MsgSolve, WsMsgOutSolution}
import models.{Solution, UserInfo}
import play.api.Configuration
import services.EulerProblemService

import scala.concurrent.{Future, TimeoutException}


/**
  * This controller creates an `Action` to handle HTTP requests for Project Euler solutions.
  * We create a Promise to return Euler problem answer from the master Actor and a new
  * actor whose sole purpose is to wait for that answer (after which it completes the promise
  * and self-destructs).
  */
@Singleton
class ProjectEulerController @Inject()(system: ActorSystem,
                                       @Named("euler-problem-master-actor") eulerProblemMaster: ActorRef,
                                       @Named("user-info-master-actor") userInfoMaster: ActorRef,
                                       configuration: Configuration,
                                       cc: ControllerComponents) extends AbstractController(cc) {
  val logger = play.api.Logger(getClass)

  def index(num: Int): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    logger.info(s"Web request for # $num")

    val userInfo: UserInfo = UserInfo(configuration, request, userInfoMaster)

    // The call to eulerProblemMaster should complete quickly since we aren't going to get the solution
    // if this problem hasn't already been solved. We'll get an "In Progress..." message instead.
    implicit val timeout: Timeout = 10.seconds
    (eulerProblemMaster ? MsgSolve(num, userInfo))
      .recoverWith{
        case to: TimeoutException =>
          logger.info(s"Timeout for # $num")
          Future(Solution.error(num, userInfo, Solution.ERROR_TIMEOUT))
        case _ =>
          logger.info(s"Unknown error for # $num")
          Future(Solution.error(num, userInfo, Solution.ERROR_OTHER))
      }
      .mapTo[Solution]
      .map(sol => Ok(WsMsgOutSolution(sol.asMine(userInfo.uuid)).toJson).withSession(userInfo.toMap.toSeq: _*))
  }

  /*
   * Old actor-less implementation
   *
    def index(num: Int) = Action.async { implicit request: Request[AnyContent] =>
      EulerProblemService.answer(num).map(result => Ok(Json.toJson(Map(num -> result))))
    }
   */
}
