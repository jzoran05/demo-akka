package sample.cluster.AkkaManagement

import akka.actor.ActorSystem
import akka.actor.Props
import akka.cluster.Cluster
import akka.management.javadsl.AkkaManagement
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import sample.cluster.actors.SimpleClusterListener


object SimpleClusterApp {

    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size == 0) startup(arrayOf("2551", "2552", "0"))
        else startup(args)
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

            val cluster = Cluster.get(system)
            system.log().info("Started [" + system + "], cluster.selfAddress = " + cluster.selfAddress() + ")")

            //#start-akka-management
            AkkaManagement.get(system).start()

            // Create an actor that handles cluster domain events
            system.actorOf(Props.create(SimpleClusterListener::class.java), "clusterListener")

            cluster.registerOnMemberUp { system.log().info("Cluster member is up!") }
        }
    }
}
