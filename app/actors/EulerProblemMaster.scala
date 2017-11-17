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
  logger.info(s"Creating Euler problem master $self")

  val maxAgeSeconds: Long =
    configuration.getOptional[Long]("project_euler.problem_max_age_seconds").getOrElse(1200)

  val cacheSolutions: Boolean =
    configuration.getOptional[Int]("project_euler.cache_solutions").getOrElse(0) > 0

  // Mutable cache of all problems that are solved or are in progress
  var solutions: scala.collection.mutable.Map[Integer, Solution] = scala.collection.mutable.Map()

  val workerRouter: Router = {
    val numWorkers: Integer = configuration.getOptional[String]("project_euler.workers_per_cpu").getOrElse("2").toInt *
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
    case MsgUserInfoUpdate(userInfo) =>
      solutions = solutions.map(s => if (s._2.by.uuid == userInfo.uuid) s._1 -> s._2.withBy(userInfo) else s._1 -> s._2)

    case MsgAllSolutions() =>
      logger.info(s"Master $self received request for all available solutions")
      // Return set of all known solutions
      sender ! solutions.filter(s => s._2.isSolved).values
    case MsgSolve(problemNumber, by) =>
      logger.info(s"Master $self received request for # $problemNumber")

      // Top-level check is whether a non-stale entry for a problem exists in the cache
      if (solutions.isDefinedAt(problemNumber) && !solutions(problemNumber).isStale(maxAgeSeconds)) {
        if (solutions(problemNumber).isSolved && cacheSolutions) {
          // If problem is solved AND caching is enabled, then we send the solution to the asker
          // and also broadcast the solution to everybody (the asker gets it again, but no biggie)
          sender ! solutions(problemNumber)
          clientBroadcaster ! MsgBroadcastSolution(solutions(problemNumber))
        } else if (!solutions(problemNumber).isSolved) {
          // If we are here that means problem is actively being solved (not stale).
          // We continue to await the solution and in meanwhile send the "In progress..." to the asker
          sender ! solutions(problemNumber)
        } else {
          // If we are here that means problem IS solved, but caching is disabled, so we solve the problem again.
          // Note that we don't necessarily always start a solution for no-cache backend -- we first make sure
          // that it's not already actively being solved! (So we don't start multiple parallel solutions for same problem)
          solutions(problemNumber) = Solution.start(problemNumber, by)
          sender ! solutions(problemNumber)
          workerRouter.route(MsgSolveWorker(problemNumber), self)
        }
      } else {
        // If the problem is not defined OR is stale, we always start its solution (regardless of caching)
        // and send "In progress..." to the asker
        solutions(problemNumber) = Solution.start(problemNumber, by)
        sender ! solutions(problemNumber)
        workerRouter.route(MsgSolveWorker(problemNumber), self)
      }

    case MsgSolution(problemNumber, answer) =>
      logger.info(s"Master $self received answer *** $answer *** for # $problemNumber")
      // We ignore any solutions that we are not aware of -- should never really happen
      if (solutions.isDefinedAt(problemNumber)) {
        // We always cache solutions -- the decision whether to use the cache is in MsgSolve logic
        solutions(problemNumber) = solutions(problemNumber).complete(answer)
        // Broadcast solution to all clients
        clientBroadcaster ! MsgBroadcastSolution(solutions(problemNumber))
      }
  }
}
