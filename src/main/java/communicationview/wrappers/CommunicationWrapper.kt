package communicationview.wrappers

import akka.actor.ActorRef

class CommunicationWrapper(val availableActors: MutableSet<String> = mutableSetOf(),
                           val commMessages: CommunicationMessages = CommunicationMessages(),
                           var globalActor: ActorRef? = null)