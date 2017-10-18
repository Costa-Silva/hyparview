package partialview.messages

import akka.actor.ActorRef
import java.io.Serializable

class ForwardJoinMessage(val newNode: ActorRef, var timeToLive: Int): Serializable