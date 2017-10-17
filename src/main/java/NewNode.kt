

import akka.actor.ActorSystem
import akkanetwork.AkkaConstants.Companion.SYSTEM_NAME
import akkanetwork.NodeID
import com.typesafe.config.ConfigFactory
import partialview.PartialViewActor

fun main(args: Array<String>) {
    val config = ConfigFactory.load()
    val system = ActorSystem.create(SYSTEM_NAME, config.getConfig("dds1"))
    val contactNode = NodeID(config.getString("dds.ip.contact"), config.getString("dds.port.contact"))
    val contact = false
    val newRef = system.actorOf(PartialViewActor.props(contactNode, contact ,5), "newActor")
}