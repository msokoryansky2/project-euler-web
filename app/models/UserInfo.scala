package models

import play.api.libs.json.{JsObject, Json}
import play.api.mvc.RequestHeader

case class UserInfo(uuid: String = "", ip: String = "", name: String = "", city: String = "", country: String = "") {
  def toJson: JsObject = Json.obj("uuid" -> uuid, "ip" -> ip, "name" -> name, "city" -> city, "country" -> country)
}

object UserInfo {
  def apply(request: RequestHeader): UserInfo =
    new UserInfo(request.remoteAddress,
      request.session.get("ip").getOrElse(""),
      request.session.get("name").getOrElse(""),
      request.session.get("city").getOrElse(""),
      request.session.get("country").getOrElse(""))
}