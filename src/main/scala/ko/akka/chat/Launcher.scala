package ko.akka.chat

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import ko.akka.chat.actors.Server

object Launcher {
  def main(args: Array[String]) {
//    val path = try {
//      args.apply(0)
//    } catch {
//      case ex : Exception => {
//        "master"
//      }
//    }
//
//    val conf = ConfigFactory.load().getConfig(path)
//    Server.props(conf)

    println("start application")
    val system1 = ActorSystem("master", ConfigFactory.load().getConfig("master"))
    system1.actorOf(Server.props(ConfigFactory.load().getConfig("master")))

    val system2 = ActorSystem("master", ConfigFactory.load().getConfig("slave"))
    system2.actorOf(Server.props(ConfigFactory.load().getConfig("slave")))
  }

}

