package modules

import actors.UserInfoMaster
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

class UserInfoMasterInjectorModule extends AbstractModule with AkkaGuiceSupport {
  def configure(): Unit = {
    bindActor[UserInfoMaster]("user-info-master-actor")
  }
}