package controllers

import javax.inject._

import actors.{MsgSolutionRequestToMaster, MsgSolutionResultToAsker}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.util.Timeout
import play.api.libs.json.Json
import play.api.mvc._
import services.EulerProblemService

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Promise
import scala.concurrent.duration._

/**
  * This controller creates an `Action` to handle HTTP requests for Project Euler solutions.
  * We create a Promise to return Euler problem answer from the master Actor and a new
  * actor whose sole purpose is to wait for that answer (after which it completes the promise
  * and self-destructs).
  */
@Singleton
class ProjectEulerController @Inject()(system: ActorSystem,
                                       @Named("euler-problem-master-actor") eulerProblemMaster: ActorRef,
                                       cc: ControllerComponents) extends AbstractController(cc) {
  val logger = play.api.Logger(getClass)

  def index(num: Int): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>

    logger.info(s"Got a request for $num")

    implicit val timeout: Timeout = Timeout(100.second)
    val p = Promise[String]
    val resultListener = system.actorOf(Props(new Actor {
      def receive: Receive = {
        case MsgSolutionResultToAsker(problemNumber, result) =>
          p.success(result)
          context.stop(self)
      }
    }))
    eulerProblemMaster.tell(msg = MsgSolutionRequestToMaster(num), sender = resultListener)
    p.future.map(result => Ok(Json.toJson(Map(num -> result))))
  }

  /*
   * Old actor-less implementation
   *

    def index(num: Int) = Action.async { implicit request: Request[AnyContent] =>
      EulerProblemService.answer(num).map(result => Ok(Json.toJson(Map(num -> result))))
    }

   *
   */
}
