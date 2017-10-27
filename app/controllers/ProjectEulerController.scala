package controllers

import javax.inject._

import actors.{MsgSolutionRequestToMaster, MsgSolutionResultToAsker}
import akka.actor.Status.Success
import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration._
import akka.pattern.ask
import play.api.Configuration
import services.EulerProblemService

import scala.concurrent.{Future, TimeoutException}
import scala.util.Failure


/**
  * This controller creates an `Action` to handle HTTP requests for Project Euler solutions.
  * We create a Promise to return Euler problem answer from the master Actor and a new
  * actor whose sole purpose is to wait for that answer (after which it completes the promise
  * and self-destructs).
  */
@Singleton
class ProjectEulerController @Inject()(system: ActorSystem,
                                       @Named("euler-problem-master-actor") eulerProblemMaster: ActorRef,
                                       configuration: Configuration,
                                       cc: ControllerComponents) extends AbstractController(cc) {
  val logger = play.api.Logger(getClass)

  def index(num: Int): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    logger.info(s"Web request for # $num")
    val maxWait: Long = configuration.getOptional[String]("project_euler.problem_max_wait_seconds").getOrElse("60").toLong
    implicit val timeout: Timeout = maxWait.seconds
    (eulerProblemMaster ?  MsgSolutionRequestToMaster(num))
      .recoverWith{
        case to: TimeoutException => {
          logger.info(s"Timeout for # $num")
          Future(MsgSolutionResultToAsker(num, "Timed Out :("))
        }
        case _ => {
          logger.info(s"Unknown error for # $num")
          Future(MsgSolutionResultToAsker(num, "Unknown Error :("))
        }
      }
      .mapTo[MsgSolutionResultToAsker]
      .map(r => Ok(Json.toJson(Map(num -> r.result))))
  }

  /*
   * Old actor-less implementation
   *
    def index(num: Int) = Action.async { implicit request: Request[AnyContent] =>
      EulerProblemService.answer(num).map(result => Ok(Json.toJson(Map(num -> result))))
    }
   */
}
