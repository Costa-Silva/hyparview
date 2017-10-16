import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import partialView.PartialViewActor

fun main(args: Array<String>) {
    //val system = ActorSystem.create(AkkaConstants.SYSTEM_NAME, ConfigFactory.load("node"))
    val system = ActorSystem.create(AkkaConstants.SYSTEM_NAME, ConfigFactory.load("contact"))

    //val myID = NodeID("127.0.0.1", "2553")

    val myID = NodeID("127.0.0.1", "2552")
    val contactNode = NodeID("127.0.0.1", "2552")
    //val newRef = system.actorOf(PartialViewActor.props(myID, contactNode, 5), "newActor")




    val contactRef = system.actorOf(PartialViewActor.props(myID, contactNode, 5), "Contact")
}