package partialview

import akka.actor.ActorPath

class PVHelpers {
    companion object {
        const val ARWL = 5
        const val PRWL = 3
        const val ACTIVE_VIEW_MAX_SIZE = 5
        const val PASSIVE_VIEW_MAX_SIZE = 15

        fun passiveViewisFull(set: Set<ActorPath>): Boolean {
            return set.size >= PASSIVE_VIEW_MAX_SIZE
        }

        fun activeViewisFull(set: Set<ActorPath>): Boolean {
            return set.size >= ACTIVE_VIEW_MAX_SIZE
        }
    }
}