package sample.cluster.transformation

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.dispatch.OnSuccess
import akka.util.Timeout
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import sample.cluster.transformation.TransformationMessages.TransformationJob
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration
import scala.concurrent.duration.FiniteDuration

import akka.pattern.Patterns.ask

object TransformationFrontendMain {

    @JvmStatic
    fun main(args: Array<String>) {
        // Override the configuration of the port when specified as program argument
        // To use artery instead of netty, change to "akka.remote.artery.canonical.port"
        // See https://doc.akka.io/docs/akka/current/remoting-artery.html for details
        val port = if (args.size > 0) args[0] else "0"
        val config = ConfigFactory.parseString(
                "akka.remote.netty.tcp.port=$port")
                .withFallback(ConfigFactory.parseString("akka.cluster.roles = [frontend]"))
                .withFallback(ConfigFactory.load())

        val system = ActorSystem.create("ClusterSystem", config)

        val frontend = system.actorOf(Props.create(TransformationFrontend::class.java), "frontend")
        val interval = Duration.create(2, TimeUnit.SECONDS)
        val timeout = Timeout(Duration.create(5, TimeUnit.SECONDS))
        val ec = system.dispatcher()
        val counter = AtomicInteger()

        system.scheduler().schedule(interval, interval, Runnable {
            ask(frontend, TransformationJob("hello-" + counter.incrementAndGet()), timeout)
                    .onSuccess(object : OnSuccess<Any>() {
                        override fun onSuccess(result: Any) {
                            println(result)
                        }
            }, ec)
        }, ec)

    }
}
