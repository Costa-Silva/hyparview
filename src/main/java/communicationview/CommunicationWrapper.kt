package communicationview

import akka.actor.ActorRef

class CommunicationWrapper(val commMessages: CommunicationMessages = CommunicationMessages(),
                           var globalActor: ActorRef? = null)