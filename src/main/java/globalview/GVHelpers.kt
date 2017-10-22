package globalview

import java.util.*

class GVHelpers {
    companion object {
        // Timers
        const val SEND_HASH_PERIOD_MS: Long = 5000
        const val SEND_EVENTS_PERIOD_MS: Long = 5000
        const val MAY_BE_DEAD_PERIOD_MS: Long = 3000

        // Global view configs
        const val PENDING_EVENTS_SIZE = 10
        const val EVENT_LIST_SIZE = 10
        const val CHECK_IF_ALIVE_TIMEOUT_MS: Long = 500

        fun pendingEventsisFull(pendingEvents: MutableMap<UUID, Event>): Boolean {
            return pendingEvents.size>= PENDING_EVENTS_SIZE
        }

        fun eventListisFull(eventList: LinkedList<UUID>): Boolean {
            return eventList.size>= EVENT_LIST_SIZE
        }
    }
}