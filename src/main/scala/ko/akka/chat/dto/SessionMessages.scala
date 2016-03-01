package ko.akka.chat.dto

import akka.util.ByteString

case class SessionMessage(from: String, byteString: ByteString)
