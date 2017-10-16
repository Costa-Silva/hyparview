package partialview

import akka.actor.ActorPath
import java.util.*

class PVHelpers {
    companion object {
        const val ARWL = 5
        const val PRWL = 3

        fun chooseRandom(set: Set<ActorPath>, withoutElement: ActorPath): ActorPath {
            var randomElement = withoutElement
            while ( randomElement == withoutElement ) {
                randomElement = set.elementAt(Random().nextInt(set.size))
            }
            return randomElement
        }
    }
}