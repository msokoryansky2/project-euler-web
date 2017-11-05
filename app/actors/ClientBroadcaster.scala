package actors

import javax.inject.Inject

import akka.actor.Actor
import akka.stream.scaladsl.SourceQueue
import messages._
import play.api.Configuration
import services.{SystemStatus, SystemStatusService}

import scala.concurrent.duration._

class ClientBroadcaster  @Inject() (configuration: Configuration) extends Actor {
  var queues: scala.collection.mutable.Set[SourceQueue[WebsocketMessage]] =
    scala.collection.mutable.Set()

  val statusFreqSeconds: Long =
    configuration.getOptional[String]("project_euler.status_update_freq_sec").getOrElse("1").toLong

  // Instantiate a system status service that sends system statuses to this actor for re-broadcast to all clients
  context.system.scheduler.schedule(statusFreqSeconds seconds, statusFreqSeconds seconds) {
    self ! SystemStatusService.status.toWsMsg
  }

  def receive: Receive = {
    case MsgRegisterWebsocketQueue(queue: SourceQueue[WebsocketMessage]) =>
      if (!queues.contains(queue)) queues += queue
    case MsgDeregisterWebsocketQueue(queue: SourceQueue[WebsocketMessage]) =>
      if (queues.contains(queue)) queues -= queue
    case MsgBroadcastSolution(solution: Solution) =>
      queues.map(_ offer solution.toWsMsg)
    case MsgBroadcastStatus(status: SystemStatus) =>
      queues.map(_ offer status.toWsMsg)
  }
}
