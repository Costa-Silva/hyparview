
import akka.actor.ActorSystem
import akkanetwork.AkkaConstants
import akkanetwork.AkkaConstants.Companion.SYSTEM_NAME
import akkanetwork.AkkaUtils
import com.typesafe.config.ConfigFactory
import globalview.GVDependenciesWrapper
import globalview.GVSharedData
import globalview.GlobalViewActor
import partialview.PartialViewActor
import partialview.wrappers.PVDependenciesWrapper
import partialview.wrappers.PVSharedData
import systemsupervisor.SystemStatus
import systemsupervisor.statuswriter.StatusActor

fun main(args: Array<String>) {
    // VS ask for MY_NODE_ID and Contact node
    if (args.size != 2) {
        System.err.println("Invalid number of args.\nUSAGE: CONTACT_NODE MY_NODE")
        System.err.println("Check application.conf for MY_NODE")
        System.exit(-1)
    }
    val contactNodeIdentifier= args[0]
    val myIdentifier = args[1]
    // Initiate akka system
    val config = ConfigFactory.load()
    val system = ActorSystem.create(SYSTEM_NAME, config.getConfig(myIdentifier))
    val contactNode = AkkaUtils.createNodeID(contactNodeIdentifier)
    val myNode = AkkaUtils.createNodeID(myIdentifier)

    if (contactNode != null && myNode != null) {

        val pvWrapper = PVDependenciesWrapper(contactNode = contactNode, myID = myNode)
        val partialRef = system.actorOf(PartialViewActor.props(pvWrapper), myIdentifier)
        val gvWrapper = GVDependenciesWrapper(nodeId = myNode, imContact = myNode==contactNode, partialActor = partialRef)
        val globalRef = system.actorOf(GlobalViewActor.props(gvWrapper), myIdentifier+ AkkaConstants.GLOBAL_ACTOR)
        pvWrapper.globalActorRef = globalRef

        val gvSharedData = GVSharedData(gvWrapper.eventList, gvWrapper.pendingEvents, gvWrapper.toRemove, gvWrapper.globalView)
        val pvSharedData = PVSharedData(myIdentifier, contactNode, pvWrapper.activeView, pvWrapper.passiveView, pvWrapper.passiveActiveView, pvWrapper.mCounter)

        val statusActor = system.actorOf(StatusActor.props(pvSharedData), myIdentifier+ AkkaConstants.STATUS_ACTOR)
        SystemStatus(system, pvSharedData, gvSharedData, statusActor)
    }
}