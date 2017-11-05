package actors

import javax.inject._

import play.api.Configuration
import akka.actor.{Actor, ActorRef, Props}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import messages._
import play.api.libs.json.{JsObject, Json}

import scala.util.Try

class Solution(val problemNumber: Integer,
               val answer: String,
               val startedAt: Long,
               val finishedAt: Long) {
  def complete(freshAnswer: String): Solution =
    new Solution(problemNumber, freshAnswer, startedAt, System.currentTimeMillis() / 1000)

  def isSolved: Boolean =
    !answer.isEmpty && Try(answer.toLong).isSuccess && finishedAt > 0

  def isStale(maxAgeSeconds: Long): Boolean =
    !isSolved && (System.currentTimeMillis() / 1000 - startedAt) > maxAgeSeconds

  def toJson: JsObject = Json.obj(
      "type" -> "solution",
      "problem_number" -> problemNumber.toString,
      "answer" -> answer
  )

  def toWsMsg: WebsocketMessageOut = WsMsgOutSolution(this)
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

class EulerProblemMaster @Inject() (configuration: Configuration,
                                    @Named("client-broadcaster-actor") clientBroadcaster: ActorRef) extends Actor {
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
    case MsgAllSolutions() =>
      // Return set of all known solutions
      sender ! solutions.filter(s => s._2.isSolved).values
    case MsgSolve(problemNumber) =>
      logger.info(s"Master $self received request for # $problemNumber")
      // Check if the problems hasn't been solved yet or is stale and if so, start a new solution
      if (!solutions.isDefinedAt(problemNumber) || solutions(problemNumber).isStale(maxAgeSeconds)) {
        solutions(problemNumber) = Solution.start(problemNumber)
        workerRouter.route(MsgSolve(problemNumber), self)
      }
      // Send either actual answer (if happens to be available) or the canned "In progress..." answer to the asker
      sender ! solutions(problemNumber)
    case MsgSolution(problemNumber, answer) =>
      // We ignore any solutions that we are not aware of -- should never really happen
      if (solutions.isDefinedAt(problemNumber)) {
        logger.info(s"Master $self received answer *** $answer *** for # $problemNumber")
        solutions(problemNumber) = solutions(problemNumber).complete(answer)
        // Broadcast solution to all clients
        clientBroadcaster ! MsgBroadcastSolution(solutions(problemNumber))
      }
  }
}
