package controllers

import java.net.URL
import javax.inject._

import akka.NotUsed
import akka.actor._
import akka.stream.scaladsl._
import play.api.libs.json._
import play.api.mvc._
import play.api.mvc.Results._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class SystemStatusController @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext) {

  val logger = play.api.Logger(getClass)

  private type WSMessage = String

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.websocket_test("ws://localhost:9000/system_status/ws"))
  }

  /**
    * Creates a websocket.  `acceptOrResult` is preferable here because it returns a
    * Future[Flow], which is required internally.
    *
    * @return a fully realized websocket.
    */
  def ws: WebSocket = WebSocket.acceptOrResult[WSMessage, WSMessage] {
    case rh if sameOriginCheck(rh) =>
      logger.info(s"Request $rh accepted")
      val out = systemStatus(rh)
      Future(Right(Flow.fromSinkAndSource(Sink.ignore, out)))
        .recover {
          case e: Exception =>
            logger.error("Cannot create websocket", e)
            val result = InternalServerError("Cannot create websocket")
            Left(result)
        }

    case rejected =>
      logger.error(s"Request $rejected failed same origin check")
      Future.successful {
        Left(Forbidden("forbidden"))
      }
  }

  /**
    * @return (Used memory, Free memory, Total memory, Free memory)
    */
  protected def systemStatusSnapshot: Long = {
    val mb = 1024 * 1024
    val r = Runtime.getRuntime
    (r.maxMemory - r.totalMemory + r.freeMemory) / mb
  }
  private def systemStatus(rh: RequestHeader) =
    Source.tick(0.5.second, 0.5.second, NotUsed).map(_ => systemStatusSnapshot.toString)

  /**
    * Checks that the WebSocket comes from the same origin.  This is necessary to protect
    * against Cross-Site WebSocket Hijacking as WebSocket does not implement Same Origin Policy.
    *
    * See https://tools.ietf.org/html/rfc6455#section-1.3 and
    * http://blog.dewhurstsecurity.com/2013/08/30/security-testing-html5-websockets.html
    */
  private def sameOriginCheck(implicit rh: RequestHeader): Boolean = {
    // The Origin header is the domain the request originates from.
    // https://tools.ietf.org/html/rfc6454#section-7
    logger.debug("Checking the ORIGIN ")

    rh.headers.get("Origin") match {
      case Some(originValue) if originMatches(originValue) =>
        logger.debug(s"originCheck: originValue = $originValue")
        true

      case Some(badOrigin) =>
        logger.error(s"originCheck: rejecting request because Origin header value $badOrigin is not in the same origin")
        false

      case None =>
        logger.error("originCheck: rejecting request because no Origin header found")
        false
    }
  }

  /**
    * Returns true if the value of the Origin header contains an acceptable value.
    */
  private def originMatches(origin: String): Boolean = {
    try {
      val url = new URL(origin)
      url.getHost == "localhost" &&
        (url.getPort match { case 9000 | 19001 => true; case _ => false })
    } catch {
      case e: Exception => false
    }
  }
}

