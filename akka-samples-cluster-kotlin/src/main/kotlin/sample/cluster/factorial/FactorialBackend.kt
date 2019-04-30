package sample.cluster.factorial

import java.math.BigInteger
import java.util.concurrent.CompletableFuture

import akka.actor.AbstractActor
import akka.pattern.PatternsCS.pipe

class FactorialBackend : AbstractActor() {

    override fun createReceive(): AbstractActor.Receive {
        return receiveBuilder()
                .match(Int::class.java) { n ->

                    val result = CompletableFuture.supplyAsync { factorial(n!!) }
                            .thenApply { factorial -> FactorialResult(n!!, factorial) }

                    pipe(result, context.dispatcher()).to(sender())

                }
                .build()
    }

    internal fun factorial(n: Int): BigInteger {
        var acc = BigInteger.ONE
        for (i in 1..n) {
            acc = acc.multiply(BigInteger.valueOf(i.toLong()))
        }
        return acc
    }
}
