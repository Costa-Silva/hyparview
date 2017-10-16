package partialview.messages

import akka.actor.ActorPath
import java.io.Serializable

class ForwardJoinMessage(val newNode: ActorPath, var timeToLive: Int): Serializable