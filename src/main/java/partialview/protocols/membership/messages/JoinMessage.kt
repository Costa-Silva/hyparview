package partialview.protocols.membership.messages

import akka.actor.ActorRef
import java.io.Serializable

class JoinMessage(val newGlobalActor: ActorRef) : Serializable