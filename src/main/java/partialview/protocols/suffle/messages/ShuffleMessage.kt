package partialview.protocols.suffle.messages

import akka.actor.ActorRef
import java.io.Serializable
import java.util.*

// TODO: TEST IF THIS SET CAN BE SERIALIZAED
class ShuffleMessage(val sample: MutableSet<ActorRef>, val timeToLive: Int, val uuid: UUID,
                     val origin: ActorRef): Serializable