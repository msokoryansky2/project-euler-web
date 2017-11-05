package messages

import actors.Solution
import akka.stream.scaladsl.SourceQueue
import services.SystemStatus

sealed trait MsgEuler
case class MsgAllSolutions() extends MsgEuler
case class MsgSolve(problemNumber: Integer) extends MsgEuler
case class MsgSolution(problemNumber: Integer, answer: String) extends MsgEuler

sealed trait MsgBroadcast
case class MsgRegisterWebsocketQueue(queue: SourceQueue[WebsocketMessage]) extends MsgBroadcast
case class MsgDeregisterWebsocketQueue(queue: SourceQueue[WebsocketMessage]) extends MsgBroadcast
case class MsgBroadcastSolution(solution: Solution) extends MsgBroadcast
case class MsgBroadcastStatus(status: SystemStatus) extends MsgBroadcast