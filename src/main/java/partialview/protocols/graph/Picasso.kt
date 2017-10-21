package systemsupervisor

import akka.actor.ActorContext
import akka.actor.ActorRef
import akkanetwork.AkkaConstants
import akkanetwork.AkkaUtils
import akkanetwork.NodeID
import com.google.gson.*
import partialview.PVDependenciesWrapper
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Paths

class WriteStatus(private val pvWrapper: PVDependenciesWrapper, private val context: ActorContext) {

    fun writeToFile() {
        val parser = JsonParser()
        val filepath = Paths.get(System.getProperty("user.dir"), "data.json").toString()
        val root = JsonObject()

        if (File(filepath).exists()) {
            val obj = parser.parse(FileReader(filepath))
            val jsonObject = obj as JsonObject

        } else {
            root.addProperty("system", AkkaConstants.SYSTEM_NAME)
            val nodesInfoArray = JsonArray()




            //nodesInfoArray.add(nodeInfo)
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

    private fun nodeInfoFor(nodeID: NodeID) {
            val actor = AkkaUtils.lookUpRemote(context, AkkaConstants.SYSTEM_NAME, nodeID)

        // mandas uma mensagem a este actor a dizer: manda-me o teu estado. (crias uma classe para englobar essa mensagem)
        // e fazes um ask em vez do tell para depois receberes a mensagem. e adicionares ao teu nodesInfoArray lá em cima
        // tens de fazer esta função returnar um node info lá para cima. para isso usas a função de baixo que te constroi o json
        // passas para la aquela mensagem com a informação que receberes.
    }

    private fun createNodeInfo() {
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
    }
}