package sample.cluster.transformation

import java.util.ArrayList
import akka.actor.AbstractActor
import akka.actor.ActorRef
import akka.actor.Terminated

import sample.cluster.transformation.TransformationMessages.JobFailed
import sample.cluster.transformation.TransformationMessages.TransformationJob
import sample.cluster.transformation.TransformationMessages.Companion.BACKEND_REGISTRATION


class TransformationFrontend : AbstractActor() {

    internal var backends: MutableList<ActorRef> = ArrayList()
    internal var jobCounter = 0

    override fun createReceive(): Receive {
        return receiveBuilder()
                .match(TransformationJob::class.java, {
                    job -> backends.isEmpty() }, { job -> sender().tell(JobFailed("Service unavailable, try again later", job), sender())
                })
                .match(TransformationJob::class.java) { job ->
                    jobCounter++
                    backends[jobCounter % backends.size].forward(job, context)
                }
                .matchEquals<Any>(BACKEND_REGISTRATION, { message ->
                    context.watch(sender())
                    backends.add(sender())
                })
                .match(Terminated::class.java) { terminated -> backends.remove(terminated.actor) }
                .build()
    }
}
