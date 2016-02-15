package durian.actors.server

import akka.actor.Actor.Receive
import akka.actor._
import akka.io._
import akka.util.ByteString
import java.net.InetSocketAddress
import Tcp._
import durian.actors.client.{Connections, Client, EchoActor}

import scala.collection.mutable.MutableList

case class Msg(msg: ByteString)

class ServerActor extends Actor with ActorLogging{

  import context.system

  override def preStart() {
    log.info("Hello I'm Server Actor!")
  }

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 8888))

  def receive = {

    case b @ Bound(localAddress) =>
      log.info("b @ Bound(localAddress)" + localAddress)

    case c @ Connected(remote, local) =>
      val handler = context.actorOf(Props[Client])

      //      val id = UUID.randomUUID().toString
      //      val handler = context.actorOf(Props(new EchoActor(id))

      // sender는
      val connection = sender()

      // clients list유지
      Connections.clients += connection
      connection ! Register(handler)

    case Msg(msg) =>
      Connections.clients.foreach { client =>
        client ! Write(msg)
      }
    case CommandFailed(_: Bind) => context stop self
      log.info("CommandFailed(_: Bind)")

    case _: AnyRef =>
      log.info("nothing special!")

  }
  
}
