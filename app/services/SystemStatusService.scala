package services

import messages.{WebsocketMessage, WsMsgStatus}
import play.api.libs.json.{JsObject, Json}

sealed case class SystemStatus(memoryUsed: Long, memoryFree: Long, memoryMax: Long) {
  def toJson: JsObject = Json.obj(
    "memoryUsed" -> memoryUsed.toString,
    "memoryFree" -> memoryFree.toString,
    "memoryMax" -> memoryMax.toString)

  def toWsMsg: WebsocketMessage = WsMsgStatus(this)
}

object SystemStatusService {
  def status: SystemStatus = {
    val mb = 1024 * 1024
    val r = Runtime.getRuntime
    SystemStatus(
      (r.totalMemory - r.freeMemory) / mb,
      (r.maxMemory - r.totalMemory + r.freeMemory) / mb,
      r.maxMemory / mb
    )
  }
}
