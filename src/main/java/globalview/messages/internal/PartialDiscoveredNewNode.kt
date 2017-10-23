package globalview.messages.internal

import akka.actor.ActorRef
import java.io.Serializable

class PartialDiscoveredNewNode(val newNode: ActorRef): Serializable