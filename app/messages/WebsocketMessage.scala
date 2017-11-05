package messages

import actors.Solution
import play.api.libs.json.{JsObject, Json}
import services.SystemStatus

sealed abstract class WebsocketMessage {
  def msgType: String
  def toJson: JsObject = Json.obj("type" -> msgType).deepMerge(toJsonPayload)
  def toJsonPayload: JsObject
}

object WebsocketMessage {
  val TYPE_SOLUTION = "solution"
  val TYPE_SOLUTIONS = "solutions"
  val TYPE_STATUS = "status"
  val TYPE_MESSAGE = "message"
  val TYPE_ERROR = "error"
}

case class WsMsgSolution(s: Solution) extends WebsocketMessage {
  override def msgType: String = WebsocketMessage.TYPE_SOLUTION
  override def toJsonPayload: JsObject = s.toJson
}

case class WsMsgSolutions(ss: Set[Solution]) extends WebsocketMessage {
  override def msgType: String = WebsocketMessage.TYPE_SOLUTIONS
  override def toJsonPayload: JsObject = Json.obj("solutions" -> ss.map(_.toJson))
}

case class WsMsgStatus(status: SystemStatus) extends WebsocketMessage {
  override def msgType: String = WebsocketMessage.TYPE_STATUS
  override def toJsonPayload: JsObject = status.toJson
}

case class WsMsgMessage(msg: String) extends WebsocketMessage {
  override def msgType: String = WebsocketMessage.TYPE_MESSAGE
  override def toJsonPayload: JsObject = Json.obj("message" -> msg)
}

case class WsMsgError(error: String) extends WebsocketMessage {
  override def msgType: String = WebsocketMessage.TYPE_STATUS
  override def toJsonPayload: JsObject = Json.obj("error" -> error)
}
