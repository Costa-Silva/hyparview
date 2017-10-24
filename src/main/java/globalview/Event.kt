package globalview

import akka.actor.ActorRef
import java.io.Serializable

class Event(val event: EventEnum, val globalNode: ActorRef, val partialNode: ActorRef): Serializable {
    override fun toString(): String {
        return "${globalNode.path().name()}-$event"
    }
}