package durian.actors.client

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging}
import akka.io.Tcp.{PeerClosed, Write, Received}
import durian.actors.server.Msg

class Client extends Actor with ActorLogging{

  override def receive: Receive = {
    case Received(data) =>
      context.parent ! Msg(data)
    case PeerClosed =>
      context stop self
    case _ : AnyRef =>
      log.info("Not Supported")
  }
}