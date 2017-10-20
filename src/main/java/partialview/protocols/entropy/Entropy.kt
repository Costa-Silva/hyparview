package partialview.protocols.entropy

import akka.actor.ActorRef
import partialview.protocols.crashrecovery.CrashRecovery

class Entropy(private var activeView: MutableSet<ActorRef>, private val crashRecovery: CrashRecovery) {

    fun cutTheWire(disconnectNodeID: String) {
        val nodeRef = activeView.first { it.path().name() == disconnectNodeID }
        crashRecovery.crashed(nodeRef)
    }

    fun kill() {
        System.err.println("Received a poison pill. It hurts soo much")
        System.exit(1)
    }
}