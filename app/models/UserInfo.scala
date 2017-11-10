package models

import play.api.libs.json.{JsObject, Json}
import play.api.mvc.RequestHeader

case class UserInfo private (uuid: String = "", ip: String = "", name: String = "", city: String = "", country: String = "") {
  def toJson: JsObject = Json.toJsObject(toMap)
  def toMap: Map[String, String] =
    Map("uuid" -> uuid, "ip" -> ip, "name" -> name, "city" -> city, "country" -> country, "desc" -> desc)
  def desc: String =
    (if (!name.isEmpty) name else "Somebody") +
      (if (!city.isEmpty) {
        if (!country.isEmpty) s" in $city, $country" else s" in $city"
      } else {
        if (!ip.isEmpty) s" at $ip" else ""
      })

  def withName(n: String): UserInfo = new UserInfo(uuid, ip, n, city, country)
  def withCity(c: String): UserInfo = new UserInfo(uuid, ip, name, c, country)
  def withCountry(c: String): UserInfo = new UserInfo(uuid, ip, name, city, c)
}

object UserInfo {
  def apply(request: RequestHeader): UserInfo =
    new UserInfo(request.session.get("uuid").getOrElse(java.util.UUID.randomUUID.toString),
      request.remoteAddress,      // always use currently known IP address
      request.session.get("name").getOrElse(""),
      request.session.get("city").getOrElse(""),
      request.session.get("country").getOrElse(""))
}