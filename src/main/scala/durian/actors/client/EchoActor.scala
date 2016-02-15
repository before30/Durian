package durian.actors.client

import akka.actor._
import akka.io.Tcp
import durian.actors.server.Msg

class EchoActor extends Actor with ActorLogging {

  import Tcp._

  def receive: Receive = {

    case Received(data) =>
      log.info("" + data)
      sender() ! Write(data)
    case PeerClosed =>
      context stop self
    case _: AnyRef =>
      log.info("You should do anything!!")
  }
}
