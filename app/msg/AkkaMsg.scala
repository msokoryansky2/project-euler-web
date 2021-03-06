package msg

import akka.actor.ActorRef
import models.{Solution, SystemStatus, UserInfo}

sealed trait MsgEuler
case class MsgAllSolutions() extends MsgEuler
case class MsgSolve(problemNumber: Integer, by: UserInfo) extends MsgEuler
case class MsgSolveWorker(problemNumber: Integer) extends MsgEuler
case class MsgSolution(problemNumber: Integer, answer: String, viaLambda: Boolean) extends MsgEuler

sealed trait MsgBroadcast
case class MsgRegisterClient(client: ActorRef) extends MsgBroadcast
case class MsgDeregisterClient(client: ActorRef) extends MsgBroadcast
case class MsgBroadcastSolution(solution: Solution) extends MsgBroadcast
case class MsgBroadcastStatus(status: SystemStatus) extends MsgBroadcast

sealed trait MsgIp2Geo
case class MsgResolveIp(uuid: String, ip: String) extends MsgIp2Geo
case class MsgIpResolution(userInfo: UserInfo) extends MsgIp2Geo

sealed trait MsgUserInfo
case class MsgUserInfoUpdate(userInfo: UserInfo) extends MsgUserInfo