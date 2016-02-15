import akka.actor.{Props, ActorSystem}
import com.typesafe.config.ConfigFactory
import durian.actors.server.ServerActor

object DurianApplication extends App {
//  val config = ConfigFactory.load()
//  val system = ActorSystem("durian", config.getConfig(""))
  val system = ActorSystem("durian")
  val serverActor = system.actorOf(Props[ServerActor], "serverActor")
}