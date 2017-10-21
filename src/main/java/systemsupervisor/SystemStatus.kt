package systemsupervisor
import akka.actor.ActorSystem
import partialview.PVDependenciesWrapper
import partialview.PVHelpers


class SystemStatus(private val pvWrapper: PVDependenciesWrapper, private val system: ActorSystem) {

    private val entropyOptions = EntropyOptions(system)

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
                "4.1" -> entropyOptions.cutTheWireOption()
                "4.2" -> entropyOptions.killOption()
                "5.1" -> WriteStatus().writeToFile(pvWrapper)
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

        println("\nSystem commands.")
        println("1)Write status to file: 5.1")
        println("\n\nType here:")
    }
}