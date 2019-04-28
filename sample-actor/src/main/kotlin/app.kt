package demo.akka.sample.main

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props

fun main() {
    val actorSystem = ActorSystem.create("sample")
    val actorRef = actorSystem.actorOf(Props.create(SampleActor::class.java))
    actorSystem.log().info("Sending message to SampleActor...")
    actorRef.tell("Hello akka", ActorRef.noSender())
}