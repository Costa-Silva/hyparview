
import akka.actor.ActorSystem
import akkanetwork.AkkaConstants.Companion.CONTACT_CONFIG
import akkanetwork.AkkaConstants.Companion.CONTACT_NODE
import akkanetwork.AkkaConstants.Companion.SYSTEM_NAME
import akkanetwork.NodeID
import com.typesafe.config.ConfigFactory
import partialview.PartialViewActor

fun main(args: Array<String>) {
    val config = ConfigFactory.load()
    val system = ActorSystem.create(SYSTEM_NAME, config.getConfig(CONTACT_CONFIG))
    val contactNode = NodeID(config.getString("dds.ip.contact"), config.getString("dds.port.contact"))
    val contact = "contact" == "contact" // I KNOW RIGHT!?
    val contactRef = system.actorOf(PartialViewActor.props(contactNode, contact ,5), CONTACT_NODE)
}