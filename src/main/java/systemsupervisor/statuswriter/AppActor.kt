package systemsupervisor.statuswriter

import akka.actor.AbstractActor
import akka.actor.Props
import systemsupervisor.graph.NodeStateMessage

class AppActor(): AbstractActor() {

    companion object {
        fun props(): Props {
            return Props.create(AppActor::class.java) { AppActor() }
        }
    }

    override fun createReceive(): Receive {
        return receiveBuilder()
                .match(NodeStateMessage::class.java) { }
                .build()
    }
}