/*
 * Copyright (C) 2017 Lightbend Inc. <http://www.lightbend.com>
 */
package akka.cluster.bootstrap.demo

import akka.NotUsed
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.PoisonPill
import akka.http.javadsl.ConnectHttp
import akka.http.javadsl.Http
import akka.http.javadsl.ServerBinding
import akka.http.javadsl.model.HttpRequest
import akka.http.javadsl.model.HttpResponse
import akka.http.javadsl.model.StatusCodes
import akka.http.javadsl.server.AllDirectives
import akka.http.javadsl.server.Route
import akka.stream.ActorMaterializer
import akka.stream.javadsl.Flow
import akka.actor.Props
import akka.cluster.Cluster
import akka.cluster.ClusterEvent
import akka.cluster.MemberStatus
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.javadsl.AkkaManagement
import akka.stream.Materializer

import java.util.HashSet
import java.util.concurrent.CompletionStage
import org.jboss.netty.util.internal.DeadLockProofWorker.start

class ClusterApp internal constructor() : AllDirectives() {

    init {
        val system = ActorSystem.create()

        val http = Http.get(system)
        val materializer = ActorMaterializer.create(system)
        val cluster = Cluster.get(system)

        system.log().info("Started [" + system + "], cluster.selfAddress = " + cluster.selfAddress() + ")")


        AkkaManagement.get(system).start()
        system.log().info("Started AkkManagement...")

        ClusterBootstrap.get(system).start()
        system.log().info("Started ClusterBootstrap...")

        cluster.subscribe(system.actorOf(Props.create(ClusterWatcher::class.java)),
                ClusterEvent.initialStateAsEvents(),
                ClusterEvent.ClusterDomainEvent::class.java)
        system.log().info("Cluster subscribe completed...")

        // create actors
        val noisyActor = createNoisyActor(system)
        system.log().info("Noisy Actor created...")

        val routeFlow = createRoutes(system, cluster).flow(system, materializer)
        system.log().info("Route Created...")

        val binding = http.bindAndHandle(routeFlow, ConnectHttp.toHost("0.0.0.0", 8080), materializer)
        system.log().info("Binding Created...")

        cluster.registerOnMemberUp({
            system.log().info("Cluster member is up!")
            noisyActor.tell(PoisonPill.getInstance(), ActorRef.noSender())
        })
        system.log().info("Cluster Registered...")

    }

    private fun createNoisyActor(system: ActorSystem): ActorRef {
        return system.actorOf(NoisyActor.props(), "NoisyActor")
    }

    private fun createRoutes(system: ActorSystem, cluster: Cluster): Route {
        val readyStates = HashSet<MemberStatus>()
        readyStates.add(MemberStatus.up())
        readyStates.add(MemberStatus.weaklyUp())

        return get { // only handle GET requests
            route(
                    path("ready"
                    ) {
                        val selfState = cluster.selfMember().status()
                        system.log().debug("ready? clusterState:$selfState")
                        if (readyStates.contains(cluster.selfMember().status()))
                            return@path complete(StatusCodes.OK)
                        else
                            return@path complete(StatusCodes.INTERNAL_SERVER_ERROR)
                    },
                    path("alive"
                    ) {
                        // When Akka HTTP can respond to requests, that is sufficient
                        // to consider ourselves 'live': we don't want K8s to kill us even
                        // when we're in the process of shutting down (only stop sending
                        // us traffic, which is done due to the readyness check then failing)
                        complete(StatusCodes.OK)
                    },
                    path("hello") { complete("<h1>Say hello to akka-http</h1>") }
            )
        }
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            ClusterApp()
        }
    }
}

