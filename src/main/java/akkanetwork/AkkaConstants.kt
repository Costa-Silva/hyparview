package akkanetwork

import java.nio.file.Paths

class AkkaConstants {
    companion object {
        const val SYSTEM_NAME = "NoFails"
        const val PARTIAL_ACTOR = ""
        const val GLOBAL_ACTOR = "global"
        const val COMM_ACTOR = "communication"
        const val STATUS_ACTOR = "status"
        val FILE_PATH = Paths.get(System.getProperty("user.dir"), "data.json").toString()
    }
}