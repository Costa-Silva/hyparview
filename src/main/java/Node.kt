
import akka.actor.ActorSystem
import akkanetwork.AkkaConstants.Companion.GLOBAL_ACTOR
import akkanetwork.AkkaConstants.Companion.STATUS_ACTOR
import akkanetwork.AkkaConstants.Companion.SYSTEM_NAME
import akkanetwork.AkkaUtils
import akkanetwork.NodeID
import com.typesafe.config.ConfigFactory
import globalview.GVDependenciesWrapper
import globalview.GlobalViewActor
import partialview.PartialViewActor
import partialview.wrappers.PVDependenciesWrapper
import partialview.wrappers.PartialViewSharedData
import systemsupervisor.SystemStatus
import systemsupervisor.statuswriter.StatusActor
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
    if(myIdentifier != "0node") {
        val contactID = "${Random().nextInt(AkkaUtils.numberFromIdentifier(myIdentifier))}node"
        contactNode = AkkaUtils.createNodeID(contactID)
    }

    AkkaUtils.createNodeID(myIdentifier)?.let {
        val gvWrapper = GVDependenciesWrapper(nodeId = it)
        val globalRef = system.actorOf(GlobalViewActor.props(gvWrapper), myIdentifier+GLOBAL_ACTOR)
        val pvWrapper = PVDependenciesWrapper(contactNode = contactNode, globalViewActor = globalRef)
        val partialViewShared = PartialViewSharedData(myIdentifier, contactNode, pvWrapper.activeView, pvWrapper.passiveView, pvWrapper.passiveActiveView, pvWrapper.mCounter)
        val partialRef = system.actorOf(PartialViewActor.props(pvWrapper), myIdentifier)

        val statusActor = system.actorOf(StatusActor.props(partialViewShared), myIdentifier+STATUS_ACTOR)
        SystemStatus(system, partialViewShared, statusActor)
    }
}