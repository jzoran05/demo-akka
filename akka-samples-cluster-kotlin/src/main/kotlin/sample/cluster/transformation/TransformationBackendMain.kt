package sample.cluster.transformation

import akka.actor.ActorSystem
import akka.actor.Props
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

object TransformationBackendMain {

    @JvmStatic
    fun main(args: Array<String>) {
        // Override the configuration of the port when specified as program argument
        // To use artery instead of netty, change to "akka.remote.artery.canonical.port"
        // See https://doc.akka.io/docs/akka/current/remoting-artery.html for details
        val port = if (args.size > 0) args[0] else "0"
        val config = ConfigFactory.parseString(
                "akka.remote.netty.tcp.port=$port")
                .withFallback(ConfigFactory.parseString("akka.cluster.roles = [backend]"))
                .withFallback(ConfigFactory.load())

        val system = ActorSystem.create("ClusterSystem", config)

        system.actorOf(Props.create(TransformationBackend::class.java), "backend")

    }

}
