package ko.akka.chat.actors

import akka.actor.{ActorSelection, ActorRef, Actor, ActorLogging}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import ko.akka.chat.ContextRoot
import ko.akka.chat.dto.SessionMessage

import scala.collection.mutable

class SimpleClusterListener extends Actor with ActorLogging {
  val cluster = Cluster(context.system)

  val sessionRoots: mutable.HashSet[ActorSelection] = mutable.HashSet.empty[ActorSelection]

  // subscribe to cluster changes, re-subscribe when restart
  override def preStart(): Unit = {
    //#subscribe
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
    //#subscribe
  }
  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive = {
    case MemberUp(member) =>
      log.info("address is {}", member.address)
      val sessionRoot = ContextRoot.system.actorSelection(member.address + "/user/sessionRoot")
      sessionRoots.+=(sessionRoot)
      log.info("Member is Up: {}", member.address)
    case UnreachableMember(member) =>
      val sessionRoot = ContextRoot.system.actorSelection(member.address + "/user/sessionRoot")
      sessionRoots.-=(sessionRoot)
      log.info("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      log.info("Member is Removed: {} after {}",
        member.address, previousStatus)
      val sessionRoot = ContextRoot.system.actorSelection(member.address + "/user/sessionRoot")
      sessionRoots.-=(sessionRoot)
    case message: SessionMessage => {
      sessionRoots.foreach(session => {
        session ! message
      })
    }

    case _: MemberEvent => // ignore

  }
}