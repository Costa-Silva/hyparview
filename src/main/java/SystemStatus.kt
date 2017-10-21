
import akka.actor.ActorSystem
import akkanetwork.AkkaConstants
import akkanetwork.AkkaUtils
import com.google.gson.*
import partialview.PVDependenciesWrapper
import partialview.PVHelpers
import partialview.protocols.entropy.EntropyActor
import partialview.protocols.entropy.EntropyOptions
import scala.util.parsing.json.JSONObject
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Paths


class SystemStatus(private val pvWrapper: PVDependenciesWrapper, private val system: ActorSystem) {

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
                "5.1" -> writeToFile()
                else -> {println("Unknown command. Usage: 1.1")}
            }
        }
    }

    private fun writeToFile() {
        val parser = JsonParser()
            val filepath = Paths.get(System.getProperty("user.dir"), "data.json").toString()

            if (File(filepath).exists()) {
                val obj = parser.parse(FileReader(filepath))
                val jsonObject = obj as JSONObject

            } else {
                val root = JsonObject()
                root.addProperty("system", AkkaConstants.SYSTEM_NAME)
                val nodesInfoArray = JsonArray()
                val nodeInfo = JsonObject()
                nodeInfo.addProperty("id", pvWrapper.myID)

                val av = JsonArray()
                pvWrapper.activeView.map { it.path().name()}.forEach {
                    av.add(JsonPrimitive(it))
                }

                val pv = JsonArray()
                pvWrapper.passiveView.map { it.path().name()}.forEach {
                    pv.add(JsonPrimitive(it))
                }
                nodeInfo.add("av", av)
                nodeInfo.add("pv", pv)
                nodeInfo.addProperty("Received",5)
                nodeInfo.addProperty("Sent",6)
                nodesInfoArray.add(nodeInfo)
                root.add("data", nodesInfoArray)
                val jsonParser = JsonParser().parse(root.toString())
                val prettyJsonString = GsonBuilder().setPrettyPrinting().create().toJson(jsonParser)

                try {
                    FileWriter(filepath).use({ file ->
                        file.write(prettyJsonString)
                        println("Saved state!")
                    })
                } catch (e: IOException) {
                    e.printStackTrace()
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