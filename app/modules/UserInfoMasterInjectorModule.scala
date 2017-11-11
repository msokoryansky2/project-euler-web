package modules

import actors.EulerProblemMaster
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

class UserInfoMasterInjectorModule extends AbstractModule with AkkaGuiceSupport {
  def configure(): Unit = {
    bindActor[EulerProblemMaster]("user-info-master-actor")
  }
}