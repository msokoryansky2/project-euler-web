package models

import msg.WsMsgOutUserLogin
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.RequestHeader

case class UserInfo private (uuid: String = "",
                             ip: String = "",
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
        else " 007")

  def withUuid(u: String): UserInfo = new UserInfo(u, ip, name, city, country, lat, long)
  def withIp(i: String): UserInfo = new UserInfo(uuid, i, name, city, country, lat, long)
  def withName(n: String): UserInfo = new UserInfo(uuid, ip, n, city, country, lat, long)
  def withCity(c: String): UserInfo = new UserInfo(uuid, ip, name, c, country, lat, long)
  def withCountry(c: String): UserInfo = new UserInfo(uuid, ip, name, city, c, lat, long)
  def withLatitude(l: String): UserInfo = new UserInfo(uuid, ip, name, city, country, l, long)
  def withLongitude(l: String): UserInfo = new UserInfo(uuid, ip, name, city, country, lat, l)
}

object UserInfo {
  def apply(request: RequestHeader): UserInfo =
    new UserInfo(request.session.get("uuid").getOrElse(java.util.UUID.randomUUID.toString),
      request.remoteAddress,      // always use currently known IP address
      request.session.get("name").getOrElse(""),
      request.session.get("city").getOrElse(""),
      request.session.get("country").getOrElse(""))

  def apply(uuid: String, ip: String): UserInfo = new UserInfo(uuid, ip)
}