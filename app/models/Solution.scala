package models

import messages.{WebsocketMessageOut, WsMsgOutSolution}
import play.api.libs.json.{JsObject, Json}

import scala.util.Try

class Solution(val problemNumber: Integer,
               val answer: String,
               val startedAt: Long,
               val finishedAt: Long,
               val by: UserInfo) {
  def complete(freshAnswer: String): Solution =
    new Solution(problemNumber, freshAnswer, startedAt, System.currentTimeMillis() / 1000, by)

  def isSolved: Boolean =
    !answer.isEmpty && Try(answer.toLong).isSuccess && finishedAt > 0

  def isStale(maxAgeSeconds: Long): Boolean =
    !isSolved && (System.currentTimeMillis() / 1000 - startedAt) > maxAgeSeconds

  def toJson: JsObject =
    Json.obj("type" -> "solution",
              "problem_number" -> problemNumber.toString,
              "answer" -> answer,
              "by" -> by.toJson)

  def toWsMsg: WebsocketMessageOut = WsMsgOutSolution(this)
}

object Solution {
  val IN_PROGRESS = "In Progress..."
  val ERROR_TIMEOUT = "Timed out :("
  val ERROR_OTHER = "Error :("
  def start(problemNumber: Integer, by: UserInfo): Solution =
    new Solution(problemNumber, IN_PROGRESS, System.currentTimeMillis() / 1000, 0, by)
  def error(problemNumber: Integer, by: UserInfo, error: String): Solution =
    new Solution(problemNumber, error,  System.currentTimeMillis() / 1000, 0, by)
}