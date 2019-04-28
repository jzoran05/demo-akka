import akka.testkit.javadsl.TestKit

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.AbstractActor
import java.time.Duration
import org.junit.jupiter.api.*

import demo.akka.sample.main.SampleActor

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestKitSampleTest  {

    class SomeActor : AbstractActor() {
        internal var target: ActorRef? = null

        override fun createReceive(): AbstractActor.Receive {
            return receiveBuilder()
                    .matchEquals(
                            "hello",
                            { message ->
                                sender.tell("world", self)
                                if (target != null) target!!.forward(message, context)
                            })
                    .match(ActorRef::class.java) {
                        actorRef -> target = actorRef
                        sender.tell("done", self)
                    }
                    .build()
        }
    }

    @Test
    fun testSomeActor() {
        /*
     * Wrap the whole test procedure within a testkit constructor
     * if you want to receive actor replies or use Within(), etc.
     */
        object : TestKit(system) {
            init {
                val props = Props.create(SomeActor::class.java)
                val subject = system!!.actorOf(props)

                // can also use JavaTestKit “from the outside”
                val probe = TestKit(system)
                // “inject” the probe by passing it to the test subject
                // like a real resource would be passed in production
                subject.tell(probe.ref, ref)
                // await the correct response
                expectMsg(Duration.ofSeconds(1), "done")

                // the run() method needs to finish within 3 seconds
                within<Any>(
                        Duration.ofSeconds(3)
                ) {
                    subject.tell("hello", ref)

                    // This is a demo: would normally use expectMsgEquals().
                    // Wait time is bounded by 3-second deadline above.
                    awaitCond(java.util.function.Supplier { probe.msgAvailable() })

                    // response must have been enqueued to us before probe
                    expectMsg(Duration.ZERO, "world")
                    // check that the probe we injected earlier got the msg
                    probe.expectMsg(Duration.ZERO, "hello")
                    Assertions.assertEquals(ref, probe.lastSender)

                    // Will wait for the rest of the 3 seconds
                    expectNoMessage()
                    null
                }
            }
        }
    }

    @Test
    fun sampleActorTest()
    {
        object : TestKit(system) {
            init {
                val props = Props.create(SampleActor::class.java)
                val subject = system!!.actorOf(props)

                val probe = TestKit(system)

                subject.tell(probe.ref, ref) // injecting the probe

                expectMsg(Duration.ofSeconds(1), "done")

                within<Any>(Duration.ofSeconds(3) ) {
                    subject.tell("message1", ref)

                    awaitCond(java.util.function.Supplier { probe.msgAvailable() })

                    expectMsg(Duration.ZERO, "response1")
                    probe.expectMsg(Duration.ZERO, "message1")
                    Assertions.assertEquals(ref, probe.lastSender)

                    expectNoMessage()
                    null
                }
            }
        }
    }

    internal var system: ActorSystem? = null

    @BeforeAll
    fun setup() {
        system = ActorSystem.create()
    }

    @AfterAll
    fun teardown() {
        TestKit.shutdownActorSystem(system)
        system = null
    }

    companion object Factory
}