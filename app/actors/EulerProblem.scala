package actors

import javax.inject._

import play.api.Configuration
import akka.actor.{Actor, ActorRef, Props}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import com.google.inject.AbstractModule
import mike.sokoryansky.EulerProblems.EulerProblem
import play.api.libs.concurrent.AkkaGuiceSupport

sealed trait MsgEuler
case class MsgSolutionRequestToMaster(problemNumber: Integer) extends MsgEuler
case class MsgSolutionRequestToWorker(problemNumber: Integer, asker: ActorRef) extends MsgEuler
case class MsgSolutionResultToMaster(problemNumber: Integer, result: String, asker: ActorRef) extends MsgEuler
case class MsgSolutionResultToAsker(problemNumber: Integer, result: String) extends MsgEuler

class EulerProblemMaster @Inject() (configuration: Configuration) extends Actor {
  val logger = play.api.Logger(getClass)

  logger.info(s"CreatingEuler problem master $self")

  val workerRouter: Router = {
    val numWorkers: Integer = 3 // configuration.getOptional[Integer]("project_euler.worker.num").getOrElse(2)
    logger.info(s"Creating $numWorkers Euler problem workers")
    val routees = Vector.fill(numWorkers) {
      val r = context.actorOf(Props[EulerProblemWorker])
      context watch r
      ActorRefRoutee(r)
    }
    Router(RoundRobinRoutingLogic(), routees)
  }

  def receive: Receive = {
    case MsgSolutionRequestToMaster(problemNumber) =>
      logger.info(s"Master received MsgSolutionRequestToMaster($problemNumber) from $sender")
      workerRouter.route(MsgSolutionRequestToWorker(problemNumber, sender), self)
    case MsgSolutionResultToMaster(problemNumber, result, asker) =>
      logger.info(s"Master received MsgSolutionResultToMaster($problemNumber, $result, $asker) from $sender")
      asker ! MsgSolutionResultToAsker(problemNumber, result)
  }
}

class EulerProblemWorker extends Actor {
  val logger = play.api.Logger(getClass)

  logger.info(s"CreatingEuler problem worker $self")

  def receive: Receive = {
    case MsgSolutionRequestToWorker(problemNumber, asker) =>
      logger.info(s"Worker received MsgSolutionRequestToWorker($problemNumber, $asker) from $sender")
      val result: String = answer(problemNumber)
      sender ! MsgSolutionResultToMaster(problemNumber, result, asker)
      logger.info(s"Worker responded with MsgSolutionResultToMaster($problemNumber, $result, $asker) to $sender")
  }

  def answer(problemNumber: Integer): String = EulerProblem(problemNumber) match {
    case Some(ep) => ep.run
    case None => s"Project Euler problem # $problemNumber is invalid or unsolved."
  }
}
