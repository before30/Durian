package durian.actors.server

import akka.actor._
import akka.io.Tcp
import akka.io.Tcp._

class EchoActor extends Actor with ActorLogging{

  import Tcp._

  def receive: Receive = {

    case Received(data) =>
      log.info("" + data)

      sender() ! Write(data)
    case PeerClosed     =>

      context stop self
    case _ : AnyRef =>
      log.info("You should do anything!!")
  }
}
