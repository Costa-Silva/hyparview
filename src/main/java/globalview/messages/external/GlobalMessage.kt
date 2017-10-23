package globalview.messages.external

import akka.actor.ActorRef
import java.io.Serializable
import java.util.*

class GlobalMessage(val globalView: MutableSet<ActorRef>, val eventList: LinkedList<UUID>): Serializable