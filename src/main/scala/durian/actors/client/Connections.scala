package durian.actors.client

import akka.actor.ActorRef

import scala.collection.mutable.MutableList

object Connections {
  val clients: MutableList[ActorRef] = MutableList.empty
}
