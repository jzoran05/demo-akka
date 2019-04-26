package demo.akka.sample.main

import akka.actor.AbstractActor
import akka.event.Logging
import akka.event.LoggingAdapter
import akka.japi.pf.ReceiveBuilder

class SampleActor : AbstractActor() {

    val log: LoggingAdapter = Logging.getLogger(context.system, this)

    override fun createReceive(): Receive {
        return ReceiveBuilder().match(String::class.java) { log.info(it) }.build()
    }



}