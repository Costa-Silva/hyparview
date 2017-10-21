package partialview

import akka.actor.ActorRef

class PVHelpers {
    companion object {
        // HyParView config
        const val ACTIVE_VIEW_MAX_SIZE = 2
        const val PASSIVE_VIEW_MAX_SIZE = 5

        // Join Protocol
        const val ARWL = 3
        const val PRWL = 1
        const val N_ACTIVE_NODES_SHUFF = 2
        const val N_PASSIVE_NODES_SHUFF = 5

        // Shuffle protocol
        const val SHUFFLE_TTL = 3
        const val TTSHUFFLE_MS: Long = 5000

        // Crash-Recovery protocol
        val ACTIVE_PASSIVE_VIEW_SIZE = Math.min(4, PASSIVE_VIEW_MAX_SIZE/2)

        fun passiveViewisFull(set: Set<ActorRef>): Boolean {
            return set.size >= PASSIVE_VIEW_MAX_SIZE
        }

        fun activeViewisFull(set: Set<ActorRef>): Boolean {
            return set.size >= ACTIVE_VIEW_MAX_SIZE
        }
    }
}