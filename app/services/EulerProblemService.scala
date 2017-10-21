package services

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import mike.sokoryansky.EulerProblems.EulerProblem

object EulerProblemService {
  /**
    * Old, actor-less way to get Euler problem solutions
    */
  def answer(num: Int): Future[String] = Future {
    EulerProblem(num) match {
      case Some(ep) => ep.run
      case None => s"Project Euler problem # $num is invalid or unsolved."
    }
  }

  def availableProblems: List[Int] =
    (1 to EulerProblem.NUMBER_PROBLEMS).toList.filter(p => EulerProblem(p).nonEmpty)
}
