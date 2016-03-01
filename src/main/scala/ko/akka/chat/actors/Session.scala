package ko.akka.chat.actors

import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator
import scala.collection.mutable

import akka.actor._
import akka.io.Tcp.{PeerClosed, Received, Write}
import akka.util.ByteString
import ko.akka.chat.dto._

object Session {
  def props(id: String, connection: ActorRef) = {
    Props(classOf[Session], id, connection)
  }
}

class Session(id: String, connection: ActorRef) extends Actor with ActorLogging {
  import DistributedPubSubMediator.{ Subscribe, SubscribeAck, Publish }

  val mediator = DistributedPubSub(context.system).mediator

  // subscribe to the topic named "content"
  mediator ! Subscribe("content", self)

  def receive: Receive = {
    case Received(data) =>
      log.info("[from {}] : {}", id, data)
      mediator ! Publish("content", SessionMessage(id, data))
    case msg: SessionMessage =>
      log.info("[{}] : {}", msg.from, msg.byteString)
      connection ! Write(ByteString("[" + msg.from + "]"))
      connection ! Write(msg.byteString)
    case SubscribeAck(Subscribe("content", None, `self`)) =>
      log.info("subscribing");
    case _ =>
      log.info("Not Supported");
  }
}
