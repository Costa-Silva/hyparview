package globalview.messages

import akka.actor.ActorRef
import java.io.Serializable

class MayBeDeadMessage(val node: ActorRef): Serializable