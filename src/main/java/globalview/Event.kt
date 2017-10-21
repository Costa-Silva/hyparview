package globalview

import akka.actor.ActorRef
import java.io.Serializable

class Event(val event: EventEnum, val node: ActorRef): Serializable