

import akka.actor.ActorSystem
import akkaNetwork.AkkaConstants
import akkaNetwork.NodeID
import com.typesafe.config.ConfigFactory
import partialview.PartialViewActor

fun main(args: Array<String>) {
    val system = ActorSystem.create(AkkaConstants.SYSTEM_NAME, ConfigFactory.load("node"))
    val myID = NodeID("127.0.0.1", "2553")
    val contactNode = NodeID("127.0.0.1", "2552")
    val newRef = system.actorOf(PartialViewActor.props(myID, contactNode, 5), "newActor")

}