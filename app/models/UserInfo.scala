package models

import akka.actor.ActorRef
import msg.{MsgIpResolution, MsgResolveIp, WsMsgOutUserLogin}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.RequestHeader
import scala.concurrent.{Await, Future}
import akka.pattern.ask
import akka.util.Timeout
import play.api.Configuration

import scala.concurrent.duration._
import scala.concurrent._
import ExecutionContext.Implicits.global

case class UserInfo private (uuid: String = "",
                             ip: String = "",
                             resolved: String = "0",
                             name: String = "",
                             city: String = "",
                             country: String = "",
                             lat: String = "",
                             long: String = "") {
  def toWsMsg: WsMsgOutUserLogin = WsMsgOutUserLogin(this)
  def toJson: JsObject = Json.toJsObject(toMap)
  def toMap: Map[String, String] =
    Map("uuid" -> uuid,
        "ip" -> ip,
        "resolved" -> resolved,
        "name" -> name,
        "city" -> city,
        "country" -> country,
        "desc" -> desc,
        "lat" -> lat,
        "long" -> long)
  def desc: String =
    (if (!name.isEmpty) name else "User") +
      (if (!city.isEmpty && !country.isEmpty) s" in $city, $country"
        else if (!country.isEmpty) s" in $country"
        else if (!city.isEmpty) s" in $city"
        else if (!ip.isEmpty) s" at $ip"
        else " Guy Incognito")

  def withUuid(u: String): UserInfo = new UserInfo(u, ip, resolved, name, city, country, lat, long)
  def withIp(i: String): UserInfo = new UserInfo(uuid, i,resolved, name, city, country, lat, long)
  def withResolved(r: String): UserInfo = new UserInfo(uuid, ip, r, name, city, country, lat, long)
  def withName(n: String): UserInfo = new UserInfo(uuid, ip, resolved, n, city, country, lat, long)
  def withCity(c: String): UserInfo = new UserInfo(uuid, ip, resolved, name, c, country, lat, long)
  def withCountry(c: String): UserInfo = new UserInfo(uuid, ip, resolved, name, city, c, lat, long)
  def withLatitude(l: String): UserInfo = new UserInfo(uuid, ip, resolved, name, city, country, l, long)
  def withLongitude(l: String): UserInfo = new UserInfo(uuid, ip, resolved, name, city, country, lat, l)
}

object UserInfo {
  /**
    * Return UserInfo from current session, if set. If not, initiate resolution of current session
    */
  def apply(config: Configuration, request: RequestHeader, userInfoMaster: ActorRef): UserInfo =
    // Start by attempting to extract UserInfo from current session
    UserInfo(config, request)
      // Fall back to requesting resolution of current session's IP
      .getOrElse{
        // Keep current session's UUID if available, even if the IP hasn't yet been resolved.
        val uuid = request.session.get("uuid").getOrElse(java.util.UUID.randomUUID.toString)
        val ip = getIp(config, request)
        // Unfortunately, block. But this should be near-instantaneous and only once per session
        implicit val timeout: Timeout = 3.seconds
        Await.result((userInfoMaster ? MsgResolveIp(uuid, ip))
          .recoverWith{
            case _ => Future(MsgIpResolution(UserInfo(uuid, ip)))
          }
          .mapTo[MsgIpResolution]
          .map(_.userInfo), 5.second)}


  /**
    * Attempt to recover UserInfo from session but only if it's resolved. None otherwise.
    * It is up to the caller to recover from Failure.
    */
  def apply(config: Configuration, request: RequestHeader): Option[UserInfo] =
    if (request.session.get("uuid").getOrElse("").isEmpty || request.session.get("resolved").getOrElse("0").toLong == 0)
      None
    else
      Some(new UserInfo(request.session.get("uuid").getOrElse(""),
            getIp(config, request),                                    // always use currently known IP address
            request.session.get("resolved").getOrElse("0"),
            request.session.get("name").getOrElse(""),
            request.session.get("city").getOrElse(""),
            request.session.get("country").getOrElse(""),
            request.session.get("lat").getOrElse(""),
            request.session.get("long").getOrElse("")))

  def apply(uuid: String, ip: String): UserInfo = new UserInfo(uuid, ip)

  /**
    * Spoof IPs if config dictates it
    */
  def getIp(config: Configuration, request: RequestHeader): String = {
    if (config.getOptional[String]("project_euler.spoof_ip").getOrElse("0").toInt > 0) {
      val r = scala.util.Random
      "" + (2 + r.nextInt(250)) + "." + (2 + r.nextInt(250)) + "." + (2 + r.nextInt(250)) + "." + (2 + r.nextInt(250))
    } else {
      request.remoteAddress
    }
  }
}
