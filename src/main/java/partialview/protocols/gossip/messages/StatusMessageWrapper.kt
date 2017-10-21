package partialview.protocols.gossip.messages

import akka.actor.ActorRef
import globalview.messages.StatusMessage
import java.io.Serializable

class StatusMessageWrapper(val statusMessage: StatusMessage, val sender: ActorRef): Serializable