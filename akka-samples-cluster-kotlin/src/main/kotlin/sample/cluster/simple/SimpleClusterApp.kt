package sample.cluster.simple

import akka.actor.ActorSystem
import akka.actor.Props
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
//import demo.akka.sample.main.SimpleClusterListener

object SimpleClusterApp {

    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size == 0)
            startup(arrayOf("2551", "2552", "0"))
        else
            startup(args)
    }

    @JvmStatic
    fun startup(ports: Array<String>) {
        for (port in ports) {
            // Override the configuration of the port
            // To use artery instead of netty, change to "akka.remote.artery.canonical.port"
            // See https://doc.akka.io/docs/akka/current/remoting-artery.html for details
            val config = ConfigFactory.parseString(
                    "akka.remote.netty.tcp.port=$port")
                    .withFallback(ConfigFactory.load())

            // Create an Akka system
            val system = ActorSystem.create("ClusterSystem", config)

            // Create an actor that handles cluster domain events
            system.actorOf(Props.create(SimpleClusterListener::class.java), "clusterListener")

        }
    }
}
