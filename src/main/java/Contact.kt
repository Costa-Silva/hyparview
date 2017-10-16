
import akka.actor.ActorSystem
import akkanetwork.AkkaConstants
import akkanetwork.NodeID
import com.typesafe.config.ConfigFactory
import partialview.PartialViewActor

fun main(args: Array<String>) {
    val system = ActorSystem.create(AkkaConstants.SYSTEM_NAME, ConfigFactory.load("contact"))
    val myID = NodeID("127.0.0.1", "2552")
    val contactNode = NodeID("127.0.0.1", "2552")
    val contactRef = system.actorOf(PartialViewActor.props(contactNode, 5), "Contact")
}