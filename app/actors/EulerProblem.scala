package actors

import javax.inject._

import play.api.Configuration
import akka.actor.{Actor, ActorRef, Props}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import services.EulerProblemService

import scala.concurrent.Future
import scala.util.{Failure, Success}

sealed trait MsgEuler
case class MsgSolutionRequestToMaster(problemNumber: Integer) extends MsgEuler
case class MsgSolutionRequestToWorker(problemNumber: Integer, asker: ActorRef) extends MsgEuler
case class MsgSolutionResultToMaster(problemNumber: Integer, result: String, asker: ActorRef) extends MsgEuler
case class MsgSolutionResultToAsker(problemNumber: Integer, result: String) extends MsgEuler

class EulerProblemMaster @Inject() (configuration: Configuration) extends Actor {
  val logger = play.api.Logger(getClass)
  logger.info(s"CreatingEuler problem master $self")

  val workerRouter: Router = {
    val numWorkers: Integer = configuration.getOptional[String]("project_euler.worker_num").getOrElse("2").toInt
    logger.info(s"Creating router with $numWorkers Euler problem workers")
    val routees = Vector.fill(numWorkers) {
      val r = context.actorOf(Props[EulerProblemWorker])
      context watch r
      ActorRefRoutee(r)
    }
    Router(RoundRobinRoutingLogic(), routees)
  }

  def receive: Receive = {
    case MsgSolutionRequestToMaster(problemNumber) =>
      logger.info(s"Master $self received request for # $problemNumber")
      workerRouter.route(MsgSolutionRequestToWorker(problemNumber, sender), self)
    case MsgSolutionResultToMaster(problemNumber, result, asker) =>
      logger.info(s"Master $self received answer *** $result *** for # $problemNumber")
      asker ! MsgSolutionResultToAsker(problemNumber, result)
  }
}

class EulerProblemWorker extends Actor {
  // EulerProblemWorker does the heavy lifting with solving PE problems, many of which will take a dozen seconds or more.
  // Because of that we do not want to use the default dispatcher but instead use a separate thread pool dispatcher.
  // This should improve responsiveness somewhat. To use the default dispatcher we would have done:
  // import context.dispatcher
  // But instead we use a dispatcher better suited for long lasting operations like PE solving:
  implicit val blockingDispatcher = context.system.dispatchers.lookup("euler-blocking-dispatcher")

  val logger = play.api.Logger(getClass)
  logger.info(s"CreatingEuler problem worker $self")

  def receive: Receive = {
    case MsgSolutionRequestToWorker(problemNumber, asker) =>
      val thisSender = sender
      logger.info(s"Worker $self received request for problem # $problemNumber")
      val futureResult: Future[String] = EulerProblemService.answer(problemNumber)
      futureResult.onComplete {
        case Success(result) =>
          thisSender ! MsgSolutionResultToMaster(problemNumber, result, asker)
        case Failure(_) =>
          thisSender ! MsgSolutionResultToMaster(problemNumber, "Can't Answer :(", asker)
      }

  }
}
