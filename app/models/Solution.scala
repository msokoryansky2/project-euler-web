package models

import msg.{WebsocketMsgOut, WsMsgOutSolution}
import play.api.libs.json.{JsObject, Json}

import scala.util.Try

class Solution private  (val problemNumber: Integer,
                         val answer: String,
                         val startedAt: Long,
                         val finishedAt: Long,
                         val by: UserInfo,
                         val mine: Boolean,
                         val viaLambda: Boolean) {
  def complete(freshAnswer: String): Solution =
    new Solution(problemNumber, freshAnswer, startedAt, System.currentTimeMillis() / 1000, by, false, false)

  def isSolved: Boolean =
    !answer.isEmpty && Try(answer.toLong).isSuccess && finishedAt > 0

  def isStale(maxAgeSeconds: Long): Boolean =
    !isSolved && (System.currentTimeMillis() / 1000 - startedAt) > maxAgeSeconds

  def isMine(myUuid: String): Boolean = myUuid == by.uuid

  // While in backend no solution is marked as "mine", but right before they are sent to the front-end
  // (either as HTTP response or websocket) they may be transformed by the sender (that knows which session/uuid
  // they "belong" to) to mark as "mine" those solutions that have the same uuid as the client.
  def asMine(myUuid: String): Solution =
    if (isMine(myUuid)) new Solution(problemNumber, answer, startedAt, finishedAt, by, true, viaLambda) else this

  def withBy(userInfo: UserInfo): Solution = new Solution(problemNumber, answer, startedAt, finishedAt, userInfo, mine, viaLambda)

  def withViaLambda(vl: Boolean): Solution = new Solution(problemNumber, answer, startedAt, finishedAt, by, mine, vl)

  def toJson: JsObject =
    Json.obj("type" -> "solution",
              "problemNumber" -> problemNumber.toString,
              "answer" -> answer,
              "by" -> by.toJson,
              "isMine" -> (if (mine) "1" else "0"),
              "viaLambda" -> (if (viaLambda) "1" else "0"),
              "duration" -> (finishedAt - startedAt)
    )

  def toWsMsg: WebsocketMsgOut = WsMsgOutSolution(this)
}

object Solution {
  val IN_PROGRESS = "In Progress..."
  val ERROR_TIMEOUT = "Timed out :("
  val ERROR_OTHER = "Error :("
  def start(problemNumber: Integer, by: UserInfo): Solution =
    new Solution(problemNumber, IN_PROGRESS, System.currentTimeMillis() / 1000, 0, by, false, false)
  def error(problemNumber: Integer, by: UserInfo, error: String): Solution =
    new Solution(problemNumber, error,  System.currentTimeMillis() / 1000, 0, by, false, false)
}