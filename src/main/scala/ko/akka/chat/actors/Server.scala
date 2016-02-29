package ko.akka.chat.actors

import java.net.InetSocketAddress
import akka.actor.{ActorRef, Props, ActorLogging, Actor}
import akka.io.Tcp._
import akka.io.{Tcp, IO}
import com.typesafe.config.Config

import scala.util.Random

object Server {
  def props(conf: Config) = {
    println(conf.getString("hostname"))
    println(conf.getInt("port"))
    Props(classOf[Server], conf)
  }
}

class Server(conf: Config) extends Actor with ActorLogging{
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress(conf.getString("hostname"), conf.getInt("port")))
  val sessionRoot = context.system.actorOf(Props[SessionRoot], "sessionRoot")
  val simpleListener = context.system.actorOf(Props[SimpleClusterListener], "listener")

  def receive = {
    case b @ Bound(localAddress) =>
      log.info("b @ Bound(localAddress)" + localAddress)

    case c @ Connected(remote, local) =>
      val id = Random.alphanumeric.take(10).mkString
      val session = context.actorOf(Session.props(id, sender(), sessionRoot, simpleListener))
      log.info("create actor for {}", id)
      val connection = sender()
      connection ! Register(session)

    case CommandFailed(_: Bind) => context stop self
      log.info("CommandFailed(_: Bind)")

    case _: AnyRef =>
      log.info("nothing special!")
  }
}
