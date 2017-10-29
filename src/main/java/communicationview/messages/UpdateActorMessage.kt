package communicationview.messages

import akka.actor.ActorPath
import communicationview.ActorUpdateEvent
import java.io.Serializable

class UpdateActorMessage(val nodePath: ActorPath, val event: ActorUpdateEvent): Serializable