/*
 * Copyright (C) 2017 Lightbend Inc. <http://www.lightbend.com>
 */
package akka.cluster.bootstrap.demo

import akka.actor.AbstractActor
import akka.cluster.Cluster
import akka.event.Logging
import akka.event.LoggingAdapter
import akka.japi.pf.ReceiveBuilder

class ClusterWatcher : AbstractActor() {
    internal var log = Logging.getLogger(context.system, this)

    internal var cluster = Cluster.get(context().system())

    override fun createReceive(): Receive {
        return ReceiveBuilder.create()
                .matchAny { msg -> log.info("Cluster " + cluster.selfAddress() + " >>> " + msg) }
                .build()
    }
}
