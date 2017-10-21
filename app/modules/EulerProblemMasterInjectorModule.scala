package modules

import actors.EulerProblemMaster
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

class EulerProblemMasterInjectorModule extends AbstractModule with AkkaGuiceSupport {
  def configure(): Unit = {
    bindActor[EulerProblemMaster]("euler-problem-master-actor")
  }
}