package systemsupervisor

import akka.actor.ActorSystem
import akkanetwork.AkkaUtils
import partialview.protocols.entropy.EntropyActor
import partialview.protocols.entropy.EntropyOptions

class EntropyOptions(val system: ActorSystem) {

    fun killOption() {
        print("NODEID: ")
        val node = readLine()
        node?.let {
            val nodeID = AkkaUtils.createNodeID(it)
            if (!nodeID.ip.isEmpty()) {
                system.actorOf(EntropyActor.props(EntropyOptions.KILL, arrayOf(nodeID)))
            } else {
                SystemStatus.printlnErr("UNKNOWN NODE!!!")
            }
        }
    }

    fun cutTheWireOption() {
        print("NODEID1 NODEID2: ")
        val nodesString = readLine()
        nodesString?.let {
            val nodes = it.split(" ")
            val nodeID0 = AkkaUtils.createNodeID(nodes[0])
            val nodeID1 = AkkaUtils.createNodeID(nodes[1])
            if (!nodeID0.ip.isEmpty() && !nodeID1.ip.isEmpty()) {
                system.actorOf(EntropyActor.props(EntropyOptions.CUT_WIRE, arrayOf(nodeID0, nodeID1)))
            } else {
                SystemStatus.printlnErr("UNKNOWN NODES!!!")
            }
        }

    }
}