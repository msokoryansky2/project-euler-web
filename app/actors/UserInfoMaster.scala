package actors

import javax.inject.{Inject, Named}

import akka.actor.{Actor, ActorRef, Props}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import models.UserInfo
import msg.{MsgIpResolution, MsgResolveIp, MsgUserInfoUpdate}
import play.api.Configuration

class UserInfoMaster @Inject() (configuration: Configuration,
                                    @Named("client-broadcaster-actor") clientBroadcaster: ActorRef) extends Actor {
  val logger = play.api.Logger(getClass)
  logger.info(s"Creating UserInfo master $self")

  // Mutable cache of all user sessions as uuid-to-UserInfo
  var sessions: scala.collection.mutable.Map[String, UserInfo] = scala.collection.mutable.Map()
  // Mutable cache of known IPs to geolocations (recycling UserInfo class with uuid being empty)
  var ip2geo: scala.collection.mutable.Map[String, UserInfo] = scala.collection.mutable.Map()

  val workerRouter: Router = {
    val numWorkers: Integer = 2

    logger.info(s"Creating router with $numWorkers IP-to-geo resolver workers")
    val routees = Vector.fill(numWorkers) {
      val r = context.actorOf(Props[UserInfoWorker])
      context watch r
      ActorRefRoutee(r)
    }
    Router(RoundRobinRoutingLogic(), routees)
  }

  def receive: Receive = {
    case MsgUserInfoUpdate(userInfo) =>
      sessions = sessions.map(s => if (s._1 == userInfo.uuid) s._1 -> userInfo else s._1 -> s._2)
      ip2geo = ip2geo.map(s => if (s._1 == userInfo.uuid) s._1 -> userInfo else s._1 -> s._2)

    case MsgResolveIp(uuid, ip) =>
      logger.info(s"Master IP-to-geo $self received request for UUID $uuid at IP $ip")
      // If we already know this uuid with this IP, return its UserInfo.
      // Otherwise create a new UserInfo entry and start the ip2geo resolution process for this IP.
      if (!sessions.isDefinedAt(uuid) || sessions(uuid).ip != ip) {
        // Check if we already have a resolution for this IP. Otherwise launch the full resolution process.
        if (ip2geo.isDefinedAt(ip)) {
          sessions(uuid) = ip2geo(ip).withUuid(uuid)
        } else {
          sessions(uuid) = UserInfo(uuid, ip)
          workerRouter.route(MsgResolveIp(uuid, ip), self)
        }
      }
      sender ! MsgIpResolution(sessions(uuid))

    case MsgIpResolution(userInfo) =>
      logger.info(s"Master IP-to-geo $self received resolution for UUID ${userInfo.uuid} at IP ${userInfo.ip}")
      // Cache the resolution if UUID and IP match and send a new User Login broadcast.
      // Ignore the resolution if either UUID or IP don't match.
      if (sessions.isDefinedAt(userInfo.uuid) && sessions(userInfo.uuid).ip == userInfo.ip) {
        sessions(userInfo.uuid) = userInfo
        // Cache the IP-to-geo resolution separately for the future
        ip2geo(userInfo.ip) = userInfo.withUuid("")
        // Broadcast the new resolution so it goes to the front-end as a "User Login" event
        clientBroadcaster ! MsgIpResolution(userInfo)
      }
  }
}