package partialview.messages

import java.io.Serializable

class BroadcastMessage(val hashValue: String, val pendingList: List<String>, val anyNodeToBeRemoved: Boolean) : Serializable