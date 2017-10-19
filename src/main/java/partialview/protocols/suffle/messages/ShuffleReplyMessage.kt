package partialview.protocols.suffle.messages

import akka.actor.ActorRef
import java.io.Serializable
import java.util.*

class ShuffleReplyMessage(val sample: MutableSet<ActorRef>, val uuid: UUID): Serializable
