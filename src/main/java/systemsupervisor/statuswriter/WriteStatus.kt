package systemsupervisor.statuswriter

import akka.actor.ActorRef
import akka.pattern.Patterns
import akkanetwork.AkkaConstants
import akkanetwork.AkkaUtils
import akkanetwork.NodeID
import com.google.gson.*
import scala.concurrent.Await
import scala.concurrent.duration.FiniteDuration
import partialview.wrappers.PartialViewSharedData
import systemsupervisor.graph.NodeStateMessage
import systemsupervisor.statuswriter.messages.RequestFromAppMessage
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

class WriteStatus {

    companion object {
        fun writeToFile(pvData: PartialViewSharedData, statusActor: ActorRef) {

            val root = JsonObject()
            root.addProperty("system", AkkaConstants.SYSTEM_NAME)
            val nodesInfoArray = JsonArray()

            for (i in 0..10000) {
                val nodeID = AkkaUtils.createNodeID("${i}node")
                if (nodeID != null) {
                    if(nodeID.identifier != pvData.identifier) {
                        val newEntry = nodeInfoFor(nodeID, statusActor)
                        if (newEntry != null) {
                            nodesInfoArray.add(newEntry)
                        } else {
                            break
                        }
                    } else {
                        nodesInfoArray.add(createNodeInfo(pvData))
                    }
                } else {
                    break
                }
            }

            try {
                root.add("data", nodesInfoArray)
                val jsonParser = JsonParser().parse(root.toString())
                val prettyJsonString = GsonBuilder().setPrettyPrinting().create().toJson(jsonParser)
                val filepath = Paths.get(System.getProperty("user.dir"), "data.json").toString()
                FileWriter(filepath).use({ file ->
                    file.write(prettyJsonString)
                    println("Saved state!")
                })
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        private fun nodeInfoFor(nodeID: NodeID, statusActor: ActorRef): JsonObject? {

            try {
                val future = Patterns.ask(statusActor, RequestFromAppMessage(nodeID.identifier),1000)
                val result = Await.result(future, FiniteDuration(1000, TimeUnit.MILLISECONDS))
                if (result != null) {
                    val message = result as NodeStateMessage
                    return createNodeInfo(message.partialViewData)
                }
            } catch (e: Exception) {
                println("deu coco @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@")
            }
            return null
        }

        private fun createNodeInfo(pvData: PartialViewSharedData): JsonObject {
            val nodeInfo = JsonObject()
            nodeInfo.addProperty("id", pvData.identifier)

            val av = JsonArray()
            pvData.activeView.map { it.path().name()}.forEach {
                av.add(JsonPrimitive(it))
            }

            val pv = JsonArray()
            pvData.passiveView.map { it.path().name()}.forEach {
                pv.add(JsonPrimitive(it))
            }
            nodeInfo.add("av", av)
            nodeInfo.add("pv", pv)
            nodeInfo.addProperty("Received",5)
            nodeInfo.addProperty("Sent",6)
            return nodeInfo
        }
    }
}