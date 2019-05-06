package sample.cluster.simple

import akka.actor.AbstractActor
import akka.cluster.Cluster
import akka.cluster.ClusterEvent
import akka.cluster.ClusterEvent.MemberEvent
import akka.cluster.ClusterEvent.MemberRemoved
import akka.cluster.ClusterEvent.MemberUp
import akka.cluster.ClusterEvent.UnreachableMember
import akka.event.Logging
import akka.event.LoggingAdapter

class SimpleClusterListener : AbstractActor() {
    internal var log = Logging.getLogger(context.system(), this)
    internal var cluster = Cluster.get(context.system())

    //subscribe to cluster changes
    override fun preStart() {
        cluster.subscribe(self(), ClusterEvent.initialStateAsEvents(),
                MemberEvent::class.java, UnreachableMember::class.java)
    }

    //re-subscribe when restart
    override fun postStop() {
        cluster.unsubscribe(self())
    }

    override fun createReceive(): Receive {
        return receiveBuilder()
                .match(MemberUp::class.java) { mUp -> log.info("Member is Up: {}", mUp.member()) }
                .match(UnreachableMember::class.java) { mUnreachable -> log.info("Member detected as unreachable: {}", mUnreachable.member()) }
                .match(MemberRemoved::class.java) { mRemoved -> log.info("Member is Removed: {}", mRemoved.member()) }
                .match(MemberEvent::class.java) { message ->
                    // ignore
                }
                .build()
    }
}
