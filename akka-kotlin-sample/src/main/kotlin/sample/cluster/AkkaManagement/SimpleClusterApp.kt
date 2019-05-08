package sample.cluster.AkkaManagement

import akka.actor.ActorSystem
import akka.actor.Props
import akka.cluster.Cluster
import akka.management.javadsl.AkkaManagement
import com.typesafe.config.ConfigFactory
import sample.cluster.actors.SimpleClusterListener

// TODO: Add Cluster Bootstap - use local config
// TODO: Add Cluster Bootstrap - use Kubernetes discovery
object SimpleClusterApp {

    /*
    args:
    1) clusterType: local, bootstrapLocalConfig
    2)
    */
    @JvmStatic
    fun main(args: Array<String>) {

        // if app is not invoked with a port parameter, then create three instances with predefined ports

        when(args.size) {
            0 -> {
                startupLocalCluster(arrayOf("2551", "2552", "2553"))
            }
            4 -> {
                startSelector(args)
            }
            else -> {
                println("Expecting 0 or 4 args... Exiting...")
                return
            }
        }


    }

    fun startSelector(args: Array<String>) {
        when(args[0]) {
            "local" -> startupLocalCluster(args.copyOfRange(1,3))
            "bootstrapLocalConfig" -> startupBootstrapLocalConfig(args.copyOfRange(1,3))
            else -> {
                println("Unsupported parameter (0) for cluster type... Exiting...")
                return
            }
        }
    }


    /*
    Starts local cluster either from IDE and predefined ports and single JVM or from terminal by providing port numbers and starting separate JVMs
    Note: Uses default application.conf file
    */
      @JvmStatic
    fun startupLocalCluster(ports: Array<String>) {
        for (port in ports) {
            // Override the configuration of the port
            // To use artery instead of netty, change to "akka.remote.artery.canonical.port"
            // See https://doc.akka.io/docs/akka/current/remoting-artery.html for details
            val managementPort = port.toInt() + 10

            val config = ConfigFactory.parseString(
                    "akka.remote.netty.tcp.port=$port")
                    .withFallback(ConfigFactory.parseString("akka.management.http.port = $managementPort"))
                    .withFallback(ConfigFactory.load("application1"))

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


    @JvmStatic
    fun startupBootstrapLocalConfig(ports: Array<String>) {
        for (port in ports) {
            // Override the configuration of the port
            // To use artery instead of netty, change to "akka.remote.artery.canonical.port"
            // See https://doc.akka.io/docs/akka/current/remoting-artery.html for details
            val managementPort = port.toInt() + 10

            val config = ConfigFactory.parseString(
                    "akka.remote.netty.tcp.port=$port")
                    .withFallback(ConfigFactory.parseString("akka.management.http.port = $managementPort"))
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
