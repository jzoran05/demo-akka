package demo.akka.sample.main

import akka.actor.AbstractActor
import akka.actor.Actor
import akka.actor.ActorContext
import akka.actor.ActorRef
import akka.event.Logging
import akka.event.LoggingAdapter
import akka.japi.pf.ReceiveBuilder
import java.io.Console

class SampleActor : AbstractActor() {

    val log: LoggingAdapter = Logging.getLogger(context.system, this)
    internal var target: ActorRef? = null

    override fun createReceive(): Receive {
        return ReceiveBuilder()
                .matchEquals(
                        "message1", { message -> message1(message) }
                )
                .matchEquals(
                        "message2", { message -> message2(message) }
                )
                .match(ActorRef::class.java)
                {
                    actorRef -> target = actorRef
                    log.info("'match' invoked.")
                    sender.tell("done", self)
                }
                .build()
    }

    private fun message1(message: String)
    {
        log.info("'message1' fun invoked. Message=$message")
        sender.tell("response1", self)
        if (target != null) target!!.forward(message, context)
    }

    private fun message2(message: String)
    {
        log.info("'message2' fun invoked. Message=$message")
        sender.tell("response2", self)
        if (target != null) target!!.forward(message, context)
    }

}
