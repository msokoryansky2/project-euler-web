package modules

import actors.ClientBroadcaster
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

class ClientBroadcasterInjectorModule extends AbstractModule with AkkaGuiceSupport {
  def configure(): Unit = {
    bindActor[ClientBroadcaster]("client-broadcaster-actor")
  }
}