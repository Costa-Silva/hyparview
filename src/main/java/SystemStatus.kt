import partialview.PVDependenciesWrapper
import partialview.PVHelpers

class SystemStatus(pvWrapper: PVDependenciesWrapper) {

    fun printlnErr(any: Any?) {
        System.err.println(any)
    }

    fun printErr(any: Any?) {
        System.err.print(any)
    }
    init {
        while (true) {
            printOptions()
            val option = readLine()
            when(option) {
                "0.1" -> printlnErr(PVHelpers.ACTIVE_VIEW_MAX_SIZE)
                "0.2" -> printlnErr(PVHelpers.PASSIVE_VIEW_MAX_SIZE)
                "1.1" -> printlnErr(pvWrapper.contactNode)
                "1.2" -> printlnErr(pvWrapper.activeView)
                "1.3" -> printlnErr(pvWrapper.passiveView)
                else -> {println("Unknown command. Usage: 1.1")}
            }
        }
    }

    private fun printOptions(){
        println("PartialView Configs.")
        println("1)Active View max size -> 0.1")
        println("2)Passive View max size -> 0.2")

        println("Partial View commands.")
        println("1)Contact node -> 1.1")
        println("2)Active View -> 1.2")
        println("3)Passive View -> 1.3")
        printErr("\nType here:")
    }
}