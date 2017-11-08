package actors

import javax.inject.{Inject, Named}

import akka.actor._
import messages._
import play.inject.Injector

class ClientHandler @Inject()(@Named("client-broadcaster-actor") clientBroadcaster: ActorRef)
                             (out: ActorRef, uuid: String) extends Actor {

  override def preStart() = {
    clientBroadcaster ! MsgRegisterClient(self)
  }

  override def postStop() = {
    clientBroadcaster ! MsgDeregisterClient(self)
  }

  def receive = {
    // Mark solutions done by me as mine and left the rest flow as is
    case msgOutSolution: WsMsgOutSolution => out ! WsMsgOutSolution(msgOutSolution.s.asMine(uuid))
    case msgOutSolutions: WsMsgOutSolutions => out ! WsMsgOutSolutions(msgOutSolutions.ss.map(s => s.asMine(uuid)))
    case msgOut: WebsocketMessageOut => out ! msgOut
  }
}

object ClientHandler {
  def props(out: ActorRef, uuid: String) = Props(new ClientHandler(Injector[ActorRef]) (out, uuid))
}
