package actors

import javax.inject.{Inject, Named}

import akka.actor._
import messages.{MsgDeregisterClient, MsgRegisterClient, WebsocketMessageOut}
import play.inject.Injector

class ClientHandler @Inject()(@Named("client-broadcaster-actor") clientBroadcaster: ActorRef)
                             (out: ActorRef) extends Actor {

  override def preStart() = {
    clientBroadcaster ! MsgRegisterClient(self)
  }

  override def postStop() = {
    clientBroadcaster ! MsgDeregisterClient(self)
  }

  def receive = {
    case msgOut: WebsocketMessageOut => out ! msgOut
  }
}

object ClientHandler {
  def props(out: ActorRef) = Props(new ClientHandler(Injector[ActorRef]) (out))
}
