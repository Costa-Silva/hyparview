
import akka.actor.ActorSystem
import akkanetwork.AkkaConstants.Companion.SYSTEM_NAME
import akkanetwork.NodeID
import com.typesafe.config.ConfigFactory
import partialview.PartialViewActor
import java.util.*

fun main(args: Array<String>) {
    // VS ask for MY_NODE_ID and Contact node
    if (args.size != 1) {
        System.err.println("Invalid number of args.\nUSAGE: MY_NODE_ID")
        System.err.println("Check application.conf for MY_NODE_ID")
        System.exit(-1)
    }
    val myIdentifier = args[0]
    // Initiate akka system
    val config = ConfigFactory.load()
    val system = ActorSystem.create(SYSTEM_NAME, config.getConfig(myIdentifier))
    var contactNode: NodeID? = null

    // Discover contact info logic
    if(myIdentifier != "node0") {
        val contactID = "node${Random().nextInt(Integer.parseInt(myIdentifier.split("node")[1]))}"

        contactNode = NodeID(config.getString("nodes.ip.$contactID"), config.getString("nodes.port.$contactID"), contactID)
    }
    val nodeRef = system.actorOf(PartialViewActor.props(contactNode ,5), myIdentifier)
}