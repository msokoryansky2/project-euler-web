package controllers

import java.net.URLDecoder
import javax.inject.{Inject, Named}

import akka.actor.ActorRef
import models.UserInfo
import msg.MsgUserInfoUpdate
import play.api.Configuration
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}

class UserInfoController @Inject()(cc: ControllerComponents,
                                   config: Configuration,
                                   @Named("user-info-master-actor") userInfoMaster: ActorRef,
                                   @Named("euler-problem-master-actor") eulerProblemMaster: ActorRef) extends AbstractController(cc) {
  val logger = play.api.Logger(getClass)
  def userInfo(name: String) = Action { implicit request: Request[AnyContent] =>
    logger.info(s"Web request for user info update")

    val sanitizedName = URLDecoder.decode(name, "UTF-8").replaceAll("[^a-zA-Z0-9'\\s]", "").take(15)
    val userInfo: UserInfo = UserInfo(config, request, userInfoMaster).withName(sanitizedName)

    userInfoMaster ! MsgUserInfoUpdate(userInfo)
    eulerProblemMaster ! MsgUserInfoUpdate(userInfo)

    Ok("ok").withSession(userInfo.toMap.toSeq: _*)
  }
}
