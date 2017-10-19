package partialview

import akka.actor.ActorRef

class PVHelpers {
    companion object {
        const val ARWL = 3
        const val PRWL = 1
        const val ACTIVE_VIEW_MAX_SIZE = 2
        const val PASSIVE_VIEW_MAX_SIZE = 5

        fun passiveViewisFull(set: Set<ActorRef>): Boolean {
            return set.size >= PASSIVE_VIEW_MAX_SIZE
        }

        fun activeViewisFull(set: Set<ActorRef>): Boolean {
            return set.size >= ACTIVE_VIEW_MAX_SIZE
        }
    }
}