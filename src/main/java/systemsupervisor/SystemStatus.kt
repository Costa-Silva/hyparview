package systemsupervisor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import globalview.GVSharedData
import partialview.PVHelpers
import partialview.wrappers.PVSharedData
import systemsupervisor.statuswriter.WriteStatus


class SystemStatus(system: ActorSystem, pvData: PVSharedData, gvData: GVSharedData,
                   statusActor: ActorRef) {

    private val entropyOptions = EntropyOptions(system)

    companion object {
        fun printlnErr(any: Any?) {
            System.err.println(any)
        }
    }

    init {
        while (true) {
            //printOptions()
            val option = readLine()
            when(option) {
                "0.1" -> printlnErr(PVHelpers.ACTIVE_VIEW_MAX_SIZE)
                "0.2" -> printlnErr(PVHelpers.PASSIVE_VIEW_MAX_SIZE)
                "0.3" -> printlnErr(PVHelpers.ACTIVE_PASSIVE_VIEW_SIZE)
                "1.1" -> printlnErr(pvData.contactNode)
                "1.2" -> printlnErr("Active View: ${pvData.activeView.map { it.path().name() }}")
                "1.3" -> printlnErr("Passive View: ${pvData.passiveView.map { it.path().name() }}")
                "1.4" -> printlnErr("Passive Active view: ${pvData.passiveActiveView.map { it.path().name() }}")
                "1.5" -> printlnErr("Received: ${pvData.mCounter.joinsReceived}")
                "1.6" -> printlnErr("Sent: ${pvData.mCounter.forwardJoinsSent} Received: ${pvData.mCounter.forwardJoinsReceived}")
                "1.7" -> printlnErr("Sent: ${pvData.mCounter.neighborRequestsSent} Received: ${pvData.mCounter.neighborRequestsReceived}")
                "1.8" -> printlnErr("Sent: ${pvData.mCounter.shufflesSent} Received: ${pvData.mCounter.shufflesReceived}")
                "1.9" -> printlnErr("Sent: ${pvData.mCounter.disconnectsSent} Received: ${pvData.mCounter.disconnectsReceived}")
                "2.1" -> printlnErr("Global view: ${gvData.globalView.map { it.key.path().name()}}")
                "2.2" -> printlnErr("Event list: ${gvData.eventList.map { it.second }}")
                "2.3" -> printlnErr("Pending events: ${gvData.pendingEvents.values}")
                "2.4" -> printlnErr("Nodes that might be dead: ${gvData.toRemove.map { it.path().name()}}")
                "2.5" -> printlnErr("Messages sent to check if alive: ${gvData.gVMCounter.messagesToCheckIfAlive}")
                "2.6" -> printlnErr("Messages sent to resolve conflicts: ${gvData.gVMCounter.messagesToResolveConflict}")
                "2.7" -> printlnErr("Broadcast messages sent by global: ${gvData.gVMCounter.messagesBroadcast}")
                "4.1" -> entropyOptions.cutTheWireOption()
                "4.2" -> entropyOptions.killOption()
                "5.1" -> WriteStatus.writeToFile(pvData, gvData.globalView, statusActor)
                else -> {println("Unknown command. Usage: 1.1")}
            }
            //Thread.sleep(2000)
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
        println("5)Joins Received -> 1.5")
        println("6)Forward joins -> 1.6")
        println("7)Neighbor requests -> 1.7")
        println("8)Shuffles -> 1.8")
        println("9)Disconnects -> 1.9")

        println("\nGlobal View commands.")
        println("1)Global view -> 2.1")
        println("2)Event list -> 2.2")
        println("3)Pending events -> 2.3")
        println("4)Nodes that might be dead -> 2.4")
        println("5)Messages sent to check if alive -> 2.5")
        println("6)Messages sent to resolve conflicts -> 2.6")

        println("\nEntropy commands.")
        println("1)Cut the wire between 2 nodes: 4.1")
        println("2)Kill a node: 4.2")

        println("\nSystem commands.")
        println("1)Write status to file: 5.1")
        println("\n\nType here:")
    }
}