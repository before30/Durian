package ko.akka.chat.actors

import java.net.InetSocketAddress
import akka.actor.{ActorRef, Props, ActorLogging, Actor}
import akka.io.Tcp._
import akka.io.{Tcp, IO}

import scala.util.Random

object Server {
  def props(hostName: String, port: Int) = {
    Props(classOf[Server], hostName, port)
  }
}

class Server(hostName: String, port: Int) extends Actor with ActorLogging{
  import context.system

  val sessionRoot = context.actorOf(Props[SessionRoot], "sessionRoot")

  IO(Tcp) ! Bind(self, new InetSocketAddress(hostName, port))

  def receive = {
    case b @ Bound(localAddress) =>
      log.info("b @ Bound(localAddress)" + localAddress)

    case c @ Connected(remote, local) =>
      val id = Random.alphanumeric.take(10).mkString
      val session = context.actorOf(Session.props(id, sender(), sessionRoot))
      log.info("create actor for {}", id)
      val connection = sender()
      connection ! Register(session)

    case CommandFailed(_: Bind) => context stop self
      log.info("CommandFailed(_: Bind)")

    case _: AnyRef =>
      log.info("nothing special!")
  }
}
