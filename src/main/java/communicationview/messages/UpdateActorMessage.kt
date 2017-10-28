package communicationview.messages

import akka.actor.ActorRef
import communicationview.ActorUpdateEvent
import java.io.Serializable

class UpdateActorMessage(val node: ActorRef, val event: ActorUpdateEvent): Serializable