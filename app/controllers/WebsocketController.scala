package controllers

import java.net.URL
import javax.inject._

import actors.ClientHandler
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.Materializer
import models.UserInfo
import play.api.libs.json._
import play.api.libs.streams.ActorFlow
import play.api.mvc.Results._
import play.api.mvc.WebSocket.MessageFlowTransformer
import play.api.mvc._
import play.api.Configuration

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WebsocketController @Inject()(cc: ControllerComponents,
                                    config: Configuration,
                                    @Named("client-broadcaster-actor") clientBroadcaster: ActorRef,
                                    @Named("user-info-master-actor") userInfoMaster: ActorRef)
                                   (implicit ec: ExecutionContext, system: ActorSystem, mat: Materializer) {
  val logger = play.api.Logger(getClass)

  implicit val transformer: MessageFlowTransformer[JsObject, JsObject] =
    MessageFlowTransformer.jsonMessageFlowTransformer[JsObject, JsObject]

  /**
    * Creates a websocket if origin check passes.
    * @return a fully realized websocket.
    */
  def ws: WebSocket = WebSocket.acceptOrResult[JsObject, JsObject] { request =>

    val userInfo: UserInfo = UserInfo(config, request, userInfoMaster)

    Future.successful({
      if (sameOriginCheck(request)) Right(ActorFlow.actorRef { out => ClientHandler.props(clientBroadcaster, out, userInfo.uuid) })
      else Left(Forbidden)
    })
  }

  /**(
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
    * For demo purposes we allow localhost Origin on any Origin hosted on amazonaws.com.
    * This is obviously not mission-critical level security but enough for a auth-less demo site.
    */
  private def originMatches(origin: String): Boolean = {
    try {
      val url = new URL(origin)
      (url.getHost == "localhost" || url.getHost.endsWith(".amazonaws.com")) &&
        (url.getPort match { case 9000 | 19001 => true; case _ => false })
    } catch {
      case e: Exception => false
    }
  }
}

