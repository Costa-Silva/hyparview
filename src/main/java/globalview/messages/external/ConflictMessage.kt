package globalview.messages.external

import akka.actor.ActorRef
import java.io.Serializable
// map of global view actor - Partial view actor
class ConflictMessage(val myGlobalView: MutableMap<ActorRef, ActorRef>): Serializable