package actors

import msg.{MsgIpResolution, MsgResolveIp}
import akka.actor.Actor
import akka.dispatch.MessageDispatcher
import models.UserInfo
import play.api.libs.json.{JsDefined, JsUndefined, Json}
import services.SimpleHttpRequest

class UserInfoWorker extends Actor {
  // UserInfoWorker does a blocking web service call, so we use a separate thread pool execution context for it.
  // This should improve responsiveness somewhat.
  //
  // To use the default dispatcher we would have done: import context.dispatcher
  implicit val blockingDispatcher: MessageDispatcher = context.system.dispatchers.lookup("ip2geo-blocking-context")

  val logger = play.api.Logger(getClass)
  logger.info(s"Creating IP-to-geo resolver worker $self")

  def getWsUrl(ip: String): String = "https://ipapi.co" + (if (ip.nonEmpty) "/" + ip  else "") + "/json/"

  def receive: Receive = {
    case MsgResolveIp(uuid, ip) =>
      logger.info(s"IP-to-geo worker $self received request for IP # $ip")
      val jsValue = Json.parse(SimpleHttpRequest.get(getWsUrl(ip)))
      val city  = jsValue \ "city" match {
        case JsDefined(v) => v.toString()
        case undefined: JsUndefined => ""
      }
      val country  = jsValue \ "country_name" match {
        case JsDefined(v) => v.toString()
        case undefined: JsUndefined => ""
      }
      val lat  = jsValue \ "latitude" match {
        case JsDefined(v) => v.toString()
        case undefined: JsUndefined => ""
      }
      val long = jsValue \ "longitude" match {
        case JsDefined(v) => v.toString()
        case undefined: JsUndefined => ""
      }
      logger.info(s"Resolved IP # $ip as city $city in country $country at $lat, $long")
      sender ! MsgIpResolution(UserInfo(uuid, ip)
        .withResolved("1").withCity(city).withCountry(country).withLatitude(lat).withLongitude(long))
  }
}
