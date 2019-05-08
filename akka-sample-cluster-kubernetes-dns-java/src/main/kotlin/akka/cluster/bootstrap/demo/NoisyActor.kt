/*
 * Copyright (C) 2017 Lightbend Inc. <http://www.lightbend.com>
 */
package akka.cluster.bootstrap.demo

import akka.actor.AbstractLoggingActor
import akka.actor.Props
import akka.japi.Creator

class NoisyActor : AbstractLoggingActor() {

    override fun preStart() {
        log().info("Noisy singleton started")
    }

    override fun postStop() {
        log().info("Noisy singleton stopped")
    }

    override fun createReceive(): Receive {
        return receiveBuilder()
                .matchAny { msg -> log().info("Msg: {}$msg") }
                .build()
    }

    companion object {

        fun props(): Props {
            return Props.create<NoisyActor>(NoisyActor::class.java, Creator<NoisyActor> { NoisyActor() })
        }
    }

}
