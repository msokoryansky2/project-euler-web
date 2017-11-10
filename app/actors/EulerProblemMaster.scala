package actors

import javax.inject._

import play.api.Configuration
import akka.actor.{Actor, ActorRef, Props}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import msg._
import models.Solution

class EulerProblemMaster @Inject() (configuration: Configuration,
                                    @Named("client-broadcaster-actor") clientBroadcaster: ActorRef) extends Actor {
  val logger = play.api.Logger(getClass)
  logger.info(s"CreatingEuler problem master $self")

  val maxAgeSeconds: Long =
    configuration.getOptional[Long]("project_euler.problem_max_age_seconds").getOrElse(1200)

  val cacheSolutions: Boolean =
    configuration.getOptional[Int]("project_euler.cache_solutions").getOrElse(0) > 0

  // Mutable cache of all problems that are solved or are in progress
  var solutions: scala.collection.mutable.Map[Integer, Solution] = scala.collection.mutable.Map()

  val workerRouter: Router = {
    val numWorkers: Integer = configuration.getOptional[String]("project_euler.workers_per_cpu").getOrElse("4").toInt *
                                Runtime.getRuntime.availableProcessors

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
      logger.info(s"Master $self received request for all available solutions")
      // Return set of all known solutions
      sender ! solutions.filter(s => s._2.isSolved).values
    case MsgSolve(problemNumber, by) =>
      logger.info(s"Master $self received request for # $problemNumber")
      // Check if the problems hasn't been solved yet or is stale and if so, start a new solution
      if (!cacheSolutions || !solutions.isDefinedAt(problemNumber) || solutions(problemNumber).isStale(maxAgeSeconds)) {
        solutions(problemNumber) = Solution.start(problemNumber, by)
        workerRouter.route(MsgSolveWorker(problemNumber), self)
      }
      // Send either actual answer (if happens to be cached) or the canned "In progress..." answer to the asker
      sender ! solutions(problemNumber)
    case MsgSolution(problemNumber, answer) =>
      logger.info(s"Master $self received answer *** $answer *** for # $problemNumber")
      // We ignore any solutions that we are not aware of -- should never really happen
      if (solutions.isDefinedAt(problemNumber)) {
        // We may or may not cache solutions, depending on config
        if (cacheSolutions) solutions(problemNumber) = solutions(problemNumber).complete(answer)
        // Broadcast solution to all clients
        clientBroadcaster ! MsgBroadcastSolution(solutions(problemNumber))
      }
  }
}
