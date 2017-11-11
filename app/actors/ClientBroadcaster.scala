package actors

import javax.inject.Inject

import akka.actor.{Actor, ActorRef}
import msg._
import models.{Solution, SystemStatus}
import play.api.Configuration

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class ClientBroadcaster  @Inject() (configuration: Configuration)
                                   (implicit ec: ExecutionContext) extends Actor {
  var clients: scala.collection.mutable.Set[ActorRef] = scala.collection.mutable.Set()

  val logger = play.api.Logger(getClass)

  val statusFreqSeconds: Long =
    configuration.getOptional[String]("project_euler.status_update_freq_sec").getOrElse("1").toLong

  // Instantiate a system status service that sends system statuses to this actor for re-broadcast to all clients
  context.system.scheduler.schedule(statusFreqSeconds seconds, statusFreqSeconds seconds) {
    self ! MsgBroadcastStatus(SystemStatus.status)
  }

  def receive: Receive = {
    case MsgRegisterClient(client: ActorRef) =>
      logger.info(s"Registered client $client")
      clients += client
    case MsgDeregisterClient(client: ActorRef) =>
      logger.info(s"Deegistered client $client")
      clients -= client
    case MsgBroadcastSolution(solution: Solution) =>
      clients.foreach(_ ! solution.toWsMsg)
    case MsgBroadcastStatus(status: SystemStatus) =>
      clients.foreach(_ ! status.toWsMsg)
  }
}
