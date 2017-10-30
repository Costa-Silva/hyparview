package testlayer.statuswriter

import akka.actor.ActorRef
import akkanetwork.AkkaConstants
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import testlayer.WriteStatus
import java.io.File
import java.io.FileReader


class WriteToFile(private val active: Boolean,
                  private val myID: String,
                  private var myStatusActor: ActorRef) {

    fun update(identifier: String = myID) {
        if (active) {
                val jsonParser = JsonParser()
                try {
                    var root = JsonObject()
                    if (!File(AkkaConstants.FILE_PATH).exists()) {
                        root.addProperty("system", AkkaConstants.SYSTEM_NAME)
                        root.add("data", JsonArray())
                    } else {
                        root = jsonParser.parse(JsonReader(FileReader(AkkaConstants.FILE_PATH))).asJsonObject
                    }
                    val arrayNodes = root.get("data").asJsonArray

                    if (identifier != myID) {
                        deleteNode(arrayNodes, identifier)
                    }

                    deleteNode(arrayNodes, myID)
                    val newNode = WriteStatus.nodeInfoFor(myID, myStatusActor)
                    arrayNodes.add(newNode)

                    WriteStatus.writeStatus(root, arrayNodes)
                } catch (e: Exception) {
                    System.err.println("Couldn't read nor write to file")
                }
        }
    }

    private fun deleteNode(arrayNodes: JsonArray, identifier: String) {
        discoverMyIndex(arrayNodes, identifier)?.let {
            arrayNodes.remove(it)
        }
    }

    private fun discoverMyIndex(arrayNodes: JsonArray, identifier: String): Int? {
        arrayNodes.forEachIndexed { index, element ->
            if (element.asJsonObject.get("id").asString == identifier) {
                return index
            }
        }
        return null
    }
}