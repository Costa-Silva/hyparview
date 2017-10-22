package globalview.messages

import globalview.Event
import java.io.Serializable
import java.util.*

class StatusMessage(val hash: Int, val pendingEvents: MutableMap<UUID, Event>, val toRemoveIsEmpty: Boolean): Serializable