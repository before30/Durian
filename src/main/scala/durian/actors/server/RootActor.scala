package durian.actors.server

import java.util

import akka.actor.Actor.Receive
import akka.actor.{ActorRef, Actor, ActorLogging}
import akka.io.Tcp
import akka.util.ByteString
import Tcp._

import scala.collection.mutable.MutableList

class RootActor extends Actor with ActorLogging {

  val list: MutableList[ActorRef] = MutableList.empty

  override def receive: Receive = {
    case ref: ActorRef =>
      log.info("add" + ref.path.name)
      list += ref
    case msg: ByteString =>
      log.info(msg.toString)
      list.foreach { con => con ! Write(msg) }
    case s : String =>
      log.info(s)
    case _ : AnyRef =>
      log.info("not found")
  }
}
