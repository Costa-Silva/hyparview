package akkanetwork

import akka.actor.ActorContext
import akka.actor.ActorRef
import akka.actor.ActorSelection
import java.util.*

class AkkaUtils {
    companion object {
        fun lookUpRemote(context: ActorContext, systemName: String, ip: NodeID, NodeName: String): ActorSelection {
            return context.actorSelection("akka.tcp://$systemName@$ip/user/$NodeName")
        }
        fun chooseRandomWithout(withoutElement: ActorRef, set: Set<ActorRef>): ActorRef? {
            if(set.contains(withoutElement) && set.size == 1 ) {
                return null
            }

            var randomElement: ActorRef? = withoutElement
            while ( randomElement == withoutElement ) {
                randomElement = chooseRandom(set)
            }
            return randomElement
        }

        fun chooseRandom(set: Set<ActorRef>): ActorRef? {
            if(set.isEmpty()) {
                return null
            }
            return set.elementAt(Random().nextInt(set.size))
        }
    }
}