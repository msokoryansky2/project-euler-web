package controllers

import javax.inject.Inject

import models.UserInfo
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}

class UserInfoController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  val logger = play.api.Logger(getClass)
  def userInfo(name: String, city: String, country: String) = Action { implicit request: Request[AnyContent] =>
    logger.info(s"Web request for user info update")
    val userInfo = UserInfo(request).withName(name).withCity(city).withCountry(country)
    Ok().withSession(userInfo.toMap.toSeq: _*)
  }
}
