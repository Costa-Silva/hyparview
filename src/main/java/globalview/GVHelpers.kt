package globalview

import java.util.*

class GVHelpers {
    companion object {
        // Timers
        const val SEND_HASH_PERIOD_MS: Long = 10000
        const val SEND_EVENTS_PERIOD_MS: Long = 5000
        const val MAY_BE_DEAD_PERIOD_MS: Long = 30000

        // Global view configs
        const val PENDING_EVENTS_SIZE = 10
        const val EVENT_LIST_SIZE = 20
        const val CHECK_IF_ALIVE_TIMEOUT_MS: Long = 500

        const val SEND_EVENTS_MESSAGE = "SendEvents"
        const val SEND_HASH_MESSAGE = "SendHash"

        fun pendingEventsisFull(pendingEvents: MutableMap<UUID, Event>): Boolean {
            return pendingEvents.size>= PENDING_EVENTS_SIZE
        }

        fun eventListisFull(eventList: LinkedList<Pair<UUID, Event>>): Boolean {
            return eventList.size>= EVENT_LIST_SIZE
        }
    }
}