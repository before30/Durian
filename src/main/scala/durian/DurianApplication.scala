import akka.actor.{Props, ActorSystem}
import com.typesafe.config.ConfigFactory
import durian.actors.server.{RootActor, ServerActor}
import ko.akka.chatt.actors.SimpleClusterListener

object DurianApplication extends App {
  val config = ConfigFactory.load()
  val system = ActorSystem("durian", config.getConfig("durian"))
//  val system = ActorSystem("durian")
  val rootActor = system.actorOf(Props[RootActor], "rootActor")

  rootActor ! "Hello"

  Console println rootActor.path
  val serverActor = system.actorOf(Props[ServerActor], "serverActor")
  val simpleListener = system.actorOf(Props[SimpleClusterListener], "listener")

}