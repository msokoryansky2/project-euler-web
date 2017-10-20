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
      logger.info(s"Master $self received request for problem # $problemNumber")
      workerRouter.route(MsgSolutionRequestToWorker(problemNumber, sender), self)
    case MsgSolutionResultToMaster(problemNumber, result, asker) =>
      logger.info(s"Master $self received result $result for problem # $problemNumber")
      asker ! MsgSolutionResultToAsker(problemNumber, result)
  }
}

class EulerProblemWorker extends Actor {
  import context.dispatcher

  val logger = play.api.Logger(getClass)
  logger.info(s"CreatingEuler problem worker $self")

  def receive: Receive = {
    case MsgSolutionRequestToWorker(problemNumber, asker) =>
      val thisSender = sender
      logger.info(s"Worker $self received request for problem # $problemNumber")
      val futureResult: Future[String] = EulerProblemService.answer(problemNumber)
      futureResult.onComplete {
        case Success(result) => thisSender ! MsgSolutionResultToMaster(problemNumber, result, asker)
        case Failure(e) => thisSender ! MsgSolutionResultToMaster(problemNumber, "We failed :(", asker)
      }
  }
}
