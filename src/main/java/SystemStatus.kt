
import akka.actor.ActorSystem
import akkanetwork.AkkaUtils
import partialview.PVDependenciesWrapper
import partialview.PVHelpers
import partialview.protocols.entropy.EntropyActor
import partialview.protocols.entropy.EntropyOptions

class SystemStatus(pvWrapper: PVDependenciesWrapper, val system: ActorSystem) {

    companion object {
        fun printlnErr(any: Any?) {
            System.err.println(any)
        }
    }

    init {
        while (true) {
            printOptions()
            val option = readLine()
            when(option) {
                "0.1" -> printlnErr(PVHelpers.ACTIVE_VIEW_MAX_SIZE)
                "0.2" -> printlnErr(PVHelpers.PASSIVE_VIEW_MAX_SIZE)
                "0.3" -> printlnErr(PVHelpers.ACTIVE_PASSIVE_VIEW_SIZE)
                "1.1" -> printlnErr(pvWrapper.contactNode)
                "1.2" -> printlnErr("Active View: ${pvWrapper.activeView.map { it.path().name() }}")
                "1.3" -> printlnErr("Passive View: ${pvWrapper.passiveView.map { it.path().name() }}")
                "1.4" -> printlnErr("Passive Active view: ${pvWrapper.passiveActiveView.map { it.path().name() }}")
                "4.1" -> cutTheWireOption()
                "4.2" -> killOption()
                else -> {println("Unknown command. Usage: 1.1")}
            }
        }
    }

    private fun printOptions(){
        println("\nPartialView Configs.")
        println("1)Active View max size -> 0.1")
        println("2)Passive View max size -> 0.2")
        println("3)Passive Active View max size -> 0.3")

        println("\nPartial View commands.")
        println("1)Contact node -> 1.1")
        println("2)Active View -> 1.2")
        println("3)Passive View -> 1.3")
        println("4)Passive Active View -> 1.4")

        println("\nEntropy commands.")
        println("1)Cut the wire between 2 nodes: 4.1")
        println("2)Kill a node: 4.2")
        println("\n\nType here:")
    }

    private fun killOption() {
        print("NODEID: ")
        val node = readLine()
        node?.let {
            val nodeID = AkkaUtils.createNodeID(it)
            if (!nodeID.ip.isEmpty()) {
                system.actorOf(EntropyActor.props(EntropyOptions.KILL, arrayOf(nodeID)))
            } else {
                printlnErr("UNKNOWN NODE!!!")
            }
        }
    }

    private fun cutTheWireOption() {
        print("NODEID1 NODEID2: ")
        val nodesString = readLine()
        nodesString?.let {
            val nodes = it.split(" ")
            val nodeID0 = AkkaUtils.createNodeID(nodes[0])
            val nodeID1 = AkkaUtils.createNodeID(nodes[1])
            if (!nodeID0.ip.isEmpty() && !nodeID1.ip.isEmpty()) {
                system.actorOf(EntropyActor.props(EntropyOptions.CUT_WIRE, arrayOf(nodeID0, nodeID1)))
            } else {
                printlnErr("UNKNOWN NODES!!!")
            }
        }

    }
}