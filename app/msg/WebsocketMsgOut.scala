package msg

import models.{Solution, SystemStatus, UserInfo}
import play.api.libs.json.{JsObject, Json}

sealed abstract class WebsocketMsgOut {
  def msgType: String
  def toJson: JsObject = Json.obj("type" -> msgType).deepMerge(toJsonPayload)
  def toJsonPayload: JsObject
}

object WebsocketMsgOut {
  val TYPE_SOLUTION = "solution"
  val TYPE_SOLUTIONS = "solutions"
  val TYPE_STATUS = "status"
  val TYPE_USER_LOGIN = "user_login"
  val TYPE_MESSAGE = "message"
  val TYPE_ERROR = "error"
}

case class WsMsgOutSolution(s: Solution) extends WebsocketMsgOut {
  override def msgType: String = WebsocketMsgOut.TYPE_SOLUTION
  override def toJsonPayload: JsObject = s.toJson
}

case class WsMsgOutSolutions(ss: Set[Solution]) extends WebsocketMsgOut {
  override def msgType: String = WebsocketMsgOut.TYPE_SOLUTIONS
  override def toJsonPayload: JsObject = Json.obj("solutions" -> ss.map(_.toJson))
}

case class WsMsgOutStatus(status: SystemStatus) extends WebsocketMsgOut {
  override def msgType: String = WebsocketMsgOut.TYPE_STATUS
  override def toJsonPayload: JsObject = status.toJson
}

case class WsMsgOutUserLogin(userInfo: UserInfo) extends WebsocketMsgOut {
  override def msgType: String = WebsocketMsgOut.TYPE_USER_LOGIN
  override def toJsonPayload: JsObject = userInfo.toJson
}

case class WsMsgOutMsg(msg: String) extends WebsocketMsgOut {
  override def msgType: String = WebsocketMsgOut.TYPE_MESSAGE
  override def toJsonPayload: JsObject = Json.obj("message" -> msg)
}

case class WsMsgOurError(error: String) extends WebsocketMsgOut {
  override def msgType: String = WebsocketMsgOut.TYPE_STATUS
  override def toJsonPayload: JsObject = Json.obj("error" -> error)
}
