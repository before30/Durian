package ko.akka.chat.actors

import akka.actor._
import akka.io.Tcp.{Write, PeerClosed, Received}
import akka.util.ByteString
import ko.akka.chat.ContextRoot
import ko.akka.chat.dto.{SessionUnsubscribe, SessionSubscribe, SessionMessage}

import scala.collection.mutable


object Session {
  def props(id: String, connection: ActorRef, sessionRootActor: ActorRef) = {
    Props(classOf[Session], id, connection, sessionRootActor)
  }
}

class Session(id: String, connection: ActorRef, sessionRoot: ActorRef) extends Actor with ActorLogging {

  override def preStart() = {
    sessionRoot ! SessionSubscribe(id, self)
  }

  override def postStop() = {
    sessionRoot ! SessionUnsubscribe(self)
  }

  def receive: Receive = {
    case Received(data) =>
      log.info("[from {}] : {}", id, data)
      ContextRoot.simpleListener ! SessionMessage(id, data)
//      sessionRoot ! SessionMessage(id, data)
    case PeerClosed =>
//      sessionRoot ! SessionMessage(id, ByteString(id + " will quit"))
      ContextRoot.simpleListener ! SessionMessage(id, ByteString(id + " will quit"))
      context stop self
    case msg: SessionMessage => {
      log.info("[{}] : {}",msg.from, msg.byteString)
      connection ! Write(ByteString("[" + msg.from + "]"))
      connection ! Write(msg.byteString)
    }
    case _ : AnyRef =>
      log.info("Not Supported")
  }
}

class SessionRoot extends Actor with ActorLogging {
  val sessions: mutable.HashMap[String, ActorRef] = mutable.HashMap.empty

  def receive: Receive = {
    case subscribe: SessionSubscribe => {
      log.info("subs {}", subscribe)
      sessions.+=((subscribe.id, subscribe.actorRef))
    }

    case unsubscribe: SessionUnsubscribe => {
      log.info("unsubs {}", unsubscribe)
      sessions.filter(pair => if (unsubscribe.actorRef == pair._2) true else false).map(pair => sessions.-=(pair._1))
    }

    case message: SessionMessage => {
      sessions.foreach(pair => if (message.from != pair._1) pair._2 ! message)
    }

    case _ =>
      log.info("Not supported messages")
  }
}