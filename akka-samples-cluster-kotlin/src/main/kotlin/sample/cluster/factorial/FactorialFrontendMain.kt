package sample.cluster.factorial


import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.Terminated
import akka.cluster.Cluster
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object FactorialFrontendMain {

    @JvmStatic
    fun main(args: Array<String>) {
        val upToN = 200
        val config = ConfigFactory.parseString("akka.cluster.roles = [frontend]").withFallback(ConfigFactory.load("factorial"))
        val system = ActorSystem.create("ClusterSystem", config)

        system.log().info("Factorials will start when 2 backend members in the cluster.")

        Cluster.get(system).registerOnMemberUp {
            system.actorOf(Props.create(FactorialFrontend::class.java, upToN, true), "factorialFrontend")
        }

        Cluster.get(system).registerOnMemberRemoved {
            // exit JVM when ActorSystem has been terminated
            val exit = Runnable { System.exit(0) }
            system.registerOnTermination(exit)

            // shut down ActorSystem
            system.terminate()

            // In case ActorSystem shutdown takes longer than 10 seconds,
            // exit the JVM forcefully anyway.
            // We must spawn a separate thread to not block current thread,
            // since that would have blocked the shutdown of the ActorSystem.
            object : Thread() {
                override fun run() {
                    try {
                        Await.ready<Terminated>(system.whenTerminated(), Duration.create(10, TimeUnit.SECONDS))
                    } catch (e: Exception) {
                        System.exit(-1)
                    }

                }
            }.start()
        }
    }

}
