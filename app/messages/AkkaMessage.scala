package messages

import actors.Solution
import akka.actor.ActorRef
import services.SystemStatus

sealed trait MsgEuler
case class MsgAllSolutions() extends MsgEuler
case class MsgSolve(problemNumber: Integer) extends MsgEuler
case class MsgSolution(problemNumber: Integer, answer: String) extends MsgEuler

sealed trait MsgBroadcast
case class MsgRegisterClient(client: ActorRef) extends MsgBroadcast
case class MsgDeregisterClient(client: ActorRef) extends MsgBroadcast
case class MsgBroadcastSolution(solution: Solution) extends MsgBroadcast
case class MsgBroadcastStatus(status: SystemStatus) extends MsgBroadcast