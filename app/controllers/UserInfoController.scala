package controllers

import javax.inject.{Inject, Named}

import akka.actor.ActorRef
import models.UserInfo
import play.api.Configuration
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}

class UserInfoController @Inject()(cc: ControllerComponents,
                                   config: Configuration,
                                   @Named("user-info-master-actor") userInfoMaster: ActorRef) extends AbstractController(cc) {
  val logger = play.api.Logger(getClass)
  def userInfo(name: String) = Action { implicit request: Request[AnyContent] =>
    logger.info(s"Web request for user info update")

    val userInfo: UserInfo = UserInfo(config, request, userInfoMaster).withName(name)

    Ok("ok").withSession(userInfo.toMap.toSeq: _*)
  }
}
