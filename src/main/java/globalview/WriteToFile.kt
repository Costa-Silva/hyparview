package globalview

import akkanetwork.AkkaConstants
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import systemsupervisor.statuswriter.WriteStatus
import java.io.File
import java.io.FileReader
import java.security.SecureRandom
import java.util.*
import kotlin.concurrent.schedule


class WriteToFile(private val active: Boolean,
                  private val myID: String,
                  private var gvWriteWrapper: GlobalWriteToFileWrapper?) {

    private var hasPendingWrite: Boolean = true
    fun update(defaultWait: Int = 0) {
        if (active) {

            val waitTime: Long = (defaultWait+SecureRandom().nextInt(2000)).toLong()
            println("vou esperar $waitTime")
            val timer = Timer("updateFile", true)
            timer.schedule(waitTime) {
                if (gvWriteWrapper != null && hasPendingWrite) {
                    hasPendingWrite = false
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

                        discoverMyIndex(arrayNodes)?.let {
                            arrayNodes.remove(it)
                        }

                        gvWriteWrapper?.let {
                            val newNode = WriteStatus.createNodeInfo(it.pvData, it.commWrapper, it.gvData)
                            arrayNodes.add(newNode)
                        }
                        WriteStatus.writeStatus(root, arrayNodes)
                    } catch (e: Exception) {
                        System.err.println("Couldn't read nor write to file")
                    }

                }else {
                    println("vou ter de trabalhar algures")
                    hasPendingWrite = true
                }
            }
        }
    }

    fun startWritting() {
        println("recebi e estou por escrever $hasPendingWrite")
        if (hasPendingWrite) {
            println("recebi e ainda é: ${gvWriteWrapper == null}")
            update(2000)
        }
    }

    private fun discoverMyIndex(arrayNodes: JsonArray): Int? {
        arrayNodes.forEachIndexed { index, element ->
            if (element.asJsonObject.get("id").asString == myID) {
                return index
            }
        }
        return null
    }
}