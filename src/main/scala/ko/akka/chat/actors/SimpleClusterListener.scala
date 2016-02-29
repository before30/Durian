package ko.akka.chat.actors

import akka.actor._
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import ko.akka.chat.dto.{MemberJoin, SessionMessage}

import scala.collection.mutable

object SimpleClusterListener {
  def props(sessionRoot: ActorRef) = {
    Props(classOf[SimpleClusterListener], sessionRoot)
  }
}

class SimpleClusterListener(sessionRoot: ActorRef) extends Actor with ActorLogging {
  val cluster = Cluster(context.system)

//  val sessionRoots: mutable.HashSet[ActorSelection] = mutable.HashSet.empty[ActorSelection]

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
      sessionRoot ! MemberJoin(member.address.toString)
      log.info("Member is Up: {}", member.address)
    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      log.info("Member is Removed: {} after {}",
        member.address, previousStatus)
    case _: MemberEvent => // ignore

  }
}