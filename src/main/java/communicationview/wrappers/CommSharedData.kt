package communicationview.wrappers

import akka.actor.ActorRef
import java.io.Serializable

class CommSharedData(val availableActors: MutableSet<String> = mutableSetOf(),
                     val commMessages: CommunicationMessages = CommunicationMessages(),
                     var globalActor: ActorRef? = null): Serializable