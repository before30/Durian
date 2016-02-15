package durian.actors.server

import java.util

import akka.actor.Actor.Receive
import akka.actor.{ActorRef, Actor, ActorLogging}
import akka.io.Tcp
import akka.util.ByteString
import Tcp._

import scala.collection.mutable.MutableList

class RootActor extends Actor with ActorLogging {

  val list: MutableList[String] = MutableList.empty

  override def receive: Receive = {
    case ref: ActorRef =>
      log.info("add" + ref.path.name)
      list += ref.path.toString
    case msg: ByteString =>
      log.info(msg.toString)
      log.info(list.toString)
      log.info(list.size.toString)
      list.foreach { path =>
        val actorref = context.system.actorSelection(path)
        actorref ! Write(msg)
//        con ! Write(msg)
      }
    case s : String =>
      log.info(s)
    case _ : AnyRef =>
      log.info("not found")
  }
}
