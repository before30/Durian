package ko.akka.chat

import akka.actor.{Props, ActorSystem}
import com.typesafe.config.ConfigFactory
import ko.akka.chat.actors.{SessionRoot, SimpleClusterListener, Server}

object Launcher {
  def main(args: Array[String]) {
    val config = ConfigFactory.load();
    val system1 = ActorSystem("joel", config.getConfig("joel"))
    val system2 = ActorSystem("yang", config.getConfig("yang"))

    val sessionRoot1 = system1.actorOf(Props[SessionRoot], "sessionRoot")
    val server1 = system1.actorOf(Server.props(config.getString("joel.hostname"), config.getInt("joel.port"), sessionRoot1))
    system1.actorOf(Props[SimpleClusterListener], "listener")

//    val sessionRoot2 = system2.actorOf(Props[SessionRoot], "sessionRoot")
//    val server2 = system2.actorOf(Server.props(config.getString("yang.hostname"), config.getInt("yang.port"), sessionRoot2))
//    system2.actorOf(Props[SimpleClusterListener], "listener")

  }
}
