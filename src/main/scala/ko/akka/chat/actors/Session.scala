package ko.akka.chat.actors

import scala.collection.mutable
import scala.concurrent.duration._
import scala.language.postfixOps

import akka.actor._
import akka.io.Tcp.{PeerClosed, Received, Write}
import akka.util.ByteString
import ko.akka.chat.dto._

object Session {
  def props(id: String, connection: ActorRef, sessionRootActor: ActorRef, simpleListener: ActorRef) = {
    Props(classOf[Session], id, connection, sessionRootActor, simpleListener)
  }
}

class Session(id: String, connection: ActorRef, sessionRoot: ActorRef, simpleListener: ActorRef) extends Actor with ActorLogging {

  override def preStart() = {
    sessionRoot ! SessionSubscribe(id, self)
  }

  override def postStop() = {
    sessionRoot ! SessionUnsubscribe(self)
  }

  def receive: Receive = {
    case Received(data) =>
      log.info("[from {}] : {}", id, data)
      sessionRoot ! SessionMessage(id, data)
    //      simpleListener ! SessionMessage(id, data)
    case PeerClosed =>
      sessionRoot ! SessionMessage(id, ByteString(id + " will quit"))
      //      simpleListener ! SessionMessage(id, ByteString(id + " will quit"))
      context stop self
    case msg: SessionMessage => {
      log.info("[{}] : {}", msg.from, msg.byteString)
      connection ! Write(ByteString("[" + msg.from + "]"))
      connection ! Write(msg.byteString)
    }
    case _: AnyRef =>
      log.info("Not Supported")
  }
}

class SessionRoot extends Actor with ActorLogging {
  val sessions: mutable.HashMap[String, ActorRef] = mutable.HashMap.empty
  val sessionRoots: mutable.Set[ActorRef] = mutable.Set.empty

  def receive: Receive = {
    case subscribe: SessionSubscribe => {
      log.info("subs {}", subscribe)
      sessions.+=((subscribe.id, subscribe.actorRef))
    }

    case unsubscribe: SessionUnsubscribe => {
      log.info("unsubs {}", unsubscribe)
      sessions.filter(pair => if (unsubscribe.actorRef == pair._2) true else false).map(pair => sessions.-=(pair._1))
    }

    case SessionMessage(from, msg) => {

      for (elem <- sessionRoots) {
        elem ! RelayMessage(from, msg)
      }
    }

    case RelayMessage(from, msg) => {
      sessions.foreach(pair => if (from != pair._1) pair._2 ! SessionMessage(from, msg))
    }

    case MemberJoin(path) => {
      log.info(s"path : $path, self.path ${self.path}")
      val sessionRoot = context.actorOf(SessionRootLookUp.props(path + "/user/sessionRoot"))
      sessionRoots.+=(sessionRoot)
    }

    case _ =>
      log.info("Not supported messages")
  }

}

object SessionRootLookUp {
  def props(path: String) = {
    Props(classOf[SessionRootLookUp], path)
  }
}

class SessionRootLookUp(path: String) extends Actor with ActorLogging {
  context.setReceiveTimeout(3 seconds)
  sendIdentifyRequest()

  def sendIdentifyRequest() = {
    val selection = context.actorSelection(path)
    selection ! Identify(path)
  }

  def receive = identify

  def identify: Receive = {

    case ActorIdentity(`path`, Some(actor)) => {
      context.setReceiveTimeout(Duration.Undefined)
      log.info("will be active state")
      context.become(active(actor))
      context.watch(actor)
    }

    case ActorIdentity(`path`, None) => {
      log.error(s"Remote actor with path $path is not available")
    }

    case ReceiveTimeout =>
      sendIdentifyRequest()

    case msg: Any =>
      log.error(s"Ignoring message $msg, not ready yet.")
  }

  def active(actor: ActorRef): Receive = {
    case Terminated(actorRef) => {
      log.info("will be identify state")
      context.become(identify)
      context.setReceiveTimeout(3 second)
      sendIdentifyRequest()
    }

    case msg: Any => actor forward msg
  }
}
