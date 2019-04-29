package sample.cluster.factorial

import java.util.concurrent.TimeUnit

import akka.actor.AbstractActor
import akka.actor.ActorRef
import akka.actor.ReceiveTimeout
import akka.event.Logging
import akka.event.LoggingAdapter
import akka.routing.FromConfig
import scala.concurrent.duration.Duration

class FactorialFrontend(internal val upToN: Int, internal val repeat: Boolean) : AbstractActor() {

    internal var log = Logging.getLogger(context.system(), this)

    internal var backend = context.actorOf(FromConfig.getInstance().props(),
            "factorialBackendRouter")

    override fun preStart() {
        sendJobs()
        context.setReceiveTimeout(Duration.create(10, TimeUnit.SECONDS))
    }

    override fun createReceive(): AbstractActor.Receive {
        return receiveBuilder()
                .match(FactorialResult::class.java) { result ->
                    if (result.n == upToN) {
                        log.debug("{}! = {}", result.n, result.factorial)
                        if (repeat)
                            sendJobs()
                        else
                            context.stop(self())
                    }
                }
                .match(ReceiveTimeout::class.java) { message ->
                    log.info("Timeout")
                    sendJobs()
                }
                .build()
    }

    internal fun sendJobs() {
        log.info("Starting batch of factorials up to [{}]", upToN)
        for (n in 1..upToN) {
            backend.tell(n, self())
        }
    }

}
