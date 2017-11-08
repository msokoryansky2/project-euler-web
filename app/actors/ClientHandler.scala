package actors

import javax.inject.{Inject, Named}

import akka.actor._
import msg._
import play.api.libs.json.JsObject
import play.api.mvc.WebSocket.MessageFlowTransformer

class ClientHandler @Inject()(@Named("client-broadcaster-actor") clientBroadcaster: ActorRef)
                             (out: ActorRef, uuid: String) extends Actor {

  implicit val transformer: MessageFlowTransformer[JsObject, JsObject] =
    MessageFlowTransformer.jsonMessageFlowTransformer[JsObject, JsObject]

  override def preStart() = {
    clientBroadcaster ! MsgRegisterClient(self)
  }

  override def postStop() = {
    clientBroadcaster ! MsgDeregisterClient(self)
  }

  def receive = {
    // Mark solutions done by me as mine and left the rest flow as is
    case msgOutSolution: WsMsgOutSolution => out ! WsMsgOutSolution(msgOutSolution.s.asMine(uuid)).toJson
    case msgOutSolutions: WsMsgOutSolutions => out ! WsMsgOutSolutions(msgOutSolutions.ss.map(s => s.asMine(uuid))).toJson
    case msgOut: WebsocketMsgOut => out ! msgOut.toJson
  }
}

object ClientHandler  {
  def props(clientBroadcaster: ActorRef, out: ActorRef, uuid: String) = Props(classOf[ClientHandler], clientBroadcaster, out, uuid)
}
