package ko.akka.chat

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import ko.akka.chat.actors.SimpleClusterListener

/**
  * Created by before30 on 2016. 2. 22..
  */
object ContextRoot {
  val config = ConfigFactory.load()
  val system = ActorSystem("joel",  config.getConfig("joel"))
  val simpleListener = system.actorOf(Props[SimpleClusterListener], "listener")
}
