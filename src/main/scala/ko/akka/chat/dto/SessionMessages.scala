package ko.akka.chat.dto

import akka.actor.ActorRef
import akka.util.ByteString

case class SessionMessage(from: String, byteString: ByteString)

case class SessionSubscribe(id: String, actorRef: ActorRef)

case class SessionUnsubscribe(actorRef: ActorRef)

case class MemberJoin(newMemeberPath: String)

case class RelayMessage(from: String, byteString: ByteString)
