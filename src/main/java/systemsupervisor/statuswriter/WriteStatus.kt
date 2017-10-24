package systemsupervisor.statuswriter

import akka.actor.ActorRef
import akka.pattern.Patterns
import akkanetwork.AkkaConstants
import akkanetwork.AkkaUtils
import akkanetwork.NodeID
import com.google.gson.*
import partialview.wrappers.PVSharedData
import scala.concurrent.Await
import scala.concurrent.duration.FiniteDuration
import systemsupervisor.graph.NodeStateMessage
import systemsupervisor.statuswriter.messages.RequestFromAppMessage
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

class WriteStatus {

    companion object {
        fun writeToFile(pvData: PVSharedData, statusActor: ActorRef) {
            val ERRORS_ALLOWED = 5
            var errorCount = 0
            val root = JsonObject()
            root.addProperty("system", AkkaConstants.SYSTEM_NAME)
            val nodesInfoArray = JsonArray()

            for (i in 0..10000) {
                if(ERRORS_ALLOWED>errorCount) {
                    val nodeID = AkkaUtils.createNodeID("${i}node")
                    if (nodeID != null) {
                        if(nodeID.identifier != pvData.identifier) {
                            val newEntry = nodeInfoFor(nodeID, statusActor)
                            if (newEntry != null) {
                                nodesInfoArray.add(newEntry)
                            } else {
                                errorCount++
                            }
                        } else {
                            nodesInfoArray.add(createNodeInfo(pvData))
                        }
                    } else {
                        break
                    }
                }else {
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
                    System.err.println("Saved state!")
                })
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        private fun nodeInfoFor(nodeID: NodeID, statusActor: ActorRef): JsonObject? {
            val timeoutTime: Long = 500
            try {
                val future = Patterns.ask(statusActor, RequestFromAppMessage(nodeID.identifier),timeoutTime)
                val result = Await.result(future, FiniteDuration(timeoutTime, TimeUnit.MILLISECONDS))
                if (result != null) {
                    val message = result as NodeStateMessage
                    return createNodeInfo(message.partialViewData)
                }
            } catch (e: Exception) { }
            return null
        }

        private fun createNodeInfo(pvData: PVSharedData): JsonObject {
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

            val mCounter = pvData.mCounter
            nodeInfo.addProperty("JoinsReceived", mCounter.joinsReceived)

            nodeInfo.addProperty("ForwardJoinsReceived", mCounter.forwardJoinsReceived)
            nodeInfo.addProperty("ForwardJoinsSent", mCounter.forwardJoinsSent)

            nodeInfo.addProperty("NeighborRequestsReceived", mCounter.neighborRequestsReceived)
            nodeInfo.addProperty("NeighborRequestsSent", mCounter.neighborRequestsSent)

            nodeInfo.addProperty("DisconnectsReceived", mCounter.disconnectsReceived)
            nodeInfo.addProperty("DisconnectsSent", mCounter.disconnectsSent)

            nodeInfo.addProperty("ShufflesReceived", mCounter.shufflesReceived)
            nodeInfo.addProperty("ShufflesSent", mCounter.shufflesSent)
            return nodeInfo
        }
    }
}