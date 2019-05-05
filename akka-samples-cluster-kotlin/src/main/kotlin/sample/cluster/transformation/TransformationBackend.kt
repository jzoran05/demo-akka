package sample.cluster.transformation

import sample.cluster.transformation.TransformationMessages.Companion.BACKEND_REGISTRATION

import akka.actor.AbstractActor
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.ClusterEvent.MemberUp
import akka.cluster.Member
import akka.cluster.MemberStatus
import sample.cluster.transformation.TransformationMessages.TransformationJob
import sample.cluster.transformation.TransformationMessages.TransformationResult

class TransformationBackend : AbstractActor() {

    internal var cluster = Cluster.get(context.system())

    //subscribe to cluster changes, MemberUp
    override fun preStart() {
        cluster.subscribe(self(), MemberUp::class.java)
    }

    //re-subscribe when restart
    override fun postStop() {
        cluster.unsubscribe(self())
    }

    override fun createReceive(): Receive {
        return receiveBuilder()
                .match(TransformationJob::class.java) { job ->
                    sender().tell(TransformationResult(job.text.toUpperCase()), self())
                }
                .match(CurrentClusterState::class.java) { state ->
                    for (member in state.members) {
                        if (member.status() == MemberStatus.up()) {
                            register(member)
                        }
                    }
                }
                .match(MemberUp::class.java) { mUp -> register(mUp.member()) }
                .build()
    }

    internal fun register(member: Member) {
        if (member.hasRole("frontend"))
            context.actorSelection(member.address().toString() + "/user/frontend").tell(BACKEND_REGISTRATION, self())
    }
}
