package messages

import models.{Solution, SystemStatus}
import play.api.libs.json.{JsObject, Json}

sealed abstract class WebsocketMessageOut {
  def msgType: String
  def toJson: JsObject = Json.obj("type" -> msgType).deepMerge(toJsonPayload)
  def toJsonPayload: JsObject
}

object WebsocketMessageOut {
  val TYPE_SOLUTION = "solution"
  val TYPE_SOLUTIONS = "solutions"
  val TYPE_STATUS = "status"
  val TYPE_MESSAGE = "message"
  val TYPE_ERROR = "error"
}

case class WsMsgOutSolution(s: Solution) extends WebsocketMessageOut {
  override def msgType: String = WebsocketMessageOut.TYPE_SOLUTION
  override def toJsonPayload: JsObject = s.toJson
}

case class WsMsgOutSolutions(ss: Set[Solution]) extends WebsocketMessageOut {
  override def msgType: String = WebsocketMessageOut.TYPE_SOLUTIONS
  override def toJsonPayload: JsObject = Json.obj("solutions" -> ss.map(_.toJson))
}

case class WsMsgOutStatus(status: SystemStatus) extends WebsocketMessageOut {
  override def msgType: String = WebsocketMessageOut.TYPE_STATUS
  override def toJsonPayload: JsObject = status.toJson
}

case class WsMsgOutMessage(msg: String) extends WebsocketMessageOut {
  override def msgType: String = WebsocketMessageOut.TYPE_MESSAGE
  override def toJsonPayload: JsObject = Json.obj("message" -> msg)
}

case class WsMsgOurError(error: String) extends WebsocketMessageOut {
  override def msgType: String = WebsocketMessageOut.TYPE_STATUS
  override def toJsonPayload: JsObject = Json.obj("error" -> error)
}
