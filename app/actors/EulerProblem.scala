package actors

import javax.inject._

import play.api.Configuration
import akka.actor.{Actor, Props}
import akka.dispatch.MessageDispatcher
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import services.EulerProblemService

sealed trait MsgEuler
case class MsgSolutionRequestToMaster(problemNumber: Integer) extends MsgEuler
case class MsgSolutionRequestToWorker(problemNumber: Integer) extends MsgEuler
case class MsgSolutionResultToMaster(problemNumber: Integer, answer: String) extends MsgEuler

class Solution(val problemNumber: Integer,
               val answer: String,
               val startedAt: Long,
               val finishedAt: Long) {
  def complete(freshAnswer: String): Solution =
    new Solution(problemNumber, freshAnswer, startedAt, System.currentTimeMillis() / 1000)

  def isSolved: Boolean =
    answer.isEmpty && answer != Solution.IN_PROGRESS && finishedAt > 0

  def isStale(maxAgeSeconds: Long): Boolean =
    !isSolved && (System.currentTimeMillis() / 1000 - startedAt) > maxAgeSeconds
}

object Solution {
  val IN_PROGRESS = "In Progress..."
  val ERROR_TIMEOUT = "Timed out :("
  val ERROR_OTHER = "Error :("
  def start(problemNumber: Integer): Solution =
    new Solution(problemNumber, IN_PROGRESS, System.currentTimeMillis() / 1000, 0)
  def error(problemNumber: Integer, error: String): Solution =
    new Solution(problemNumber, error,  System.currentTimeMillis() / 1000, 0)
}

class EulerProblemMaster @Inject() (configuration: Configuration) extends Actor {
  val logger = play.api.Logger(getClass)
  logger.info(s"CreatingEuler problem master $self")

  val maxAgeSeconds: Long =
    configuration.getOptional[String]("project_euler.problem_max_age_seconds").getOrElse("1200").toLong

  // Mutable cache of all problems that are solved or are in progress
  var solutions: scala.collection.mutable.Map[Integer, Solution] = scala.collection.mutable.Map()

  val workerRouter: Router = {
    val numWorkers: Integer = configuration.getOptional[String]("project_euler.worker_num").getOrElse("8").toInt
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
      // Check if the problems hasn't been solved yet or is stale and if so, start a new solution
      if (!solutions.isDefinedAt(problemNumber) || solutions(problemNumber).isStale(maxAgeSeconds)) {
        solutions(problemNumber) = Solution.start(problemNumber)
        workerRouter.route(MsgSolutionRequestToWorker(problemNumber), self)
      }
      // Send either actual answer (if happens to be available) or the canned "In progress..." answer to the asker
      sender ! solutions(problemNumber)
    case MsgSolutionResultToMaster(problemNumber, answer) =>
      // We ignore any solutions that we are not aware of -- should never really happen
      if (solutions.isDefinedAt(problemNumber)) {
        logger.info(s"Master $self received answer *** $answer *** for # $problemNumber")
        solutions(problemNumber) = solutions(problemNumber).complete(answer)
      }
  }
}

class EulerProblemWorker extends Actor {
  // EulerProblemWorker does the heavy lifting with solving PE problems, many of which will take a dozen seconds or more.
  // Because of that we do not want to use the default dispatcher but instead use a separate thread pool dispatcher.
  // This should improve responsiveness somewhat.
  //
  // To use the default dispatcher we would have done: import context.dispatcher
  implicit val blockingDispatcher: MessageDispatcher = context.system.dispatchers.lookup("euler-blocking-context")

  val logger = play.api.Logger(getClass)
  logger.info(s"CreatingEuler problem worker $self")

  def receive: Receive = {
    case MsgSolutionRequestToWorker(problemNumber) =>
      logger.info(s"Worker $self received request for problem # $problemNumber")
      sender ! MsgSolutionResultToMaster(problemNumber, EulerProblemService.answer(problemNumber))
  }
}
