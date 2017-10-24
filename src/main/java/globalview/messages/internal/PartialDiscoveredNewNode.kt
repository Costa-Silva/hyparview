package globalview.messages.internal

import akka.actor.ActorRef
import java.io.Serializable

class PartialDiscoveredNewNode(val newGlobalNode: ActorRef): Serializable