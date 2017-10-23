package partialview.protocols.suffle.messages

import akka.actor.ActorRef
import java.io.Serializable
import java.util.*

class ShuffleMessage(val sample: MutableSet<ActorRef>, val timeToLive: Int, val uuid: UUID,
                     val origin: ActorRef): Serializable