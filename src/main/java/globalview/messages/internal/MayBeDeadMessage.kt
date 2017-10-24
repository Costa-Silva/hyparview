package globalview.messages.internal

import akka.actor.ActorRef
import java.io.Serializable

class MayBeDeadMessage(val partialNode: ActorRef): Serializable