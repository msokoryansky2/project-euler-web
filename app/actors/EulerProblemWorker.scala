package actors

import messages.{MsgSolution, MsgSolveWorker}
import akka.actor.Actor
import akka.dispatch.MessageDispatcher
import services.EulerProblemService


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
    case MsgSolveWorker(problemNumber) =>
      logger.info(s"Worker $self received request for problem # $problemNumber")
      sender ! MsgSolution(problemNumber, EulerProblemService.answer(problemNumber))
  }
}
