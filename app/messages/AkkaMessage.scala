package messages

import akka.actor.ActorRef
import models.{Solution, SystemStatus, UserInfo}

sealed trait MsgEuler
case class MsgAllSolutions() extends MsgEuler
case class MsgSolve(problemNumber: Integer, by: UserInfo) extends MsgEuler
case class MsgSolveWorker(problemNumber: Integer) extends MsgEuler
case class MsgSolution(problemNumber: Integer, answer: String) extends MsgEuler

sealed trait MsgBroadcast
case class MsgRegisterClient(client: ActorRef) extends MsgBroadcast
case class MsgDeregisterClient(client: ActorRef) extends MsgBroadcast
case class MsgBroadcastSolution(solution: Solution) extends MsgBroadcast
case class MsgBroadcastStatus(status: SystemStatus) extends MsgBroadcast