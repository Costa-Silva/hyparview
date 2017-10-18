package akkanetwork

import akka.actor.ActorContext
import akka.actor.ActorPath
import akka.actor.ActorSelection
import java.util.*

class AkkaUtils {
    companion object {
        fun lookUpRemote(context: ActorContext, systemName: String, ip: NodeID, NodeName: String): ActorSelection {
            return context.actorSelection("akka.tcp://$systemName@$ip/user/$NodeName")
        }
        fun chooseRandomWithout(withoutElement: ActorPath, set: Set<ActorPath>): ActorPath {
            var randomElement = withoutElement
            while ( randomElement == withoutElement ) {
                randomElement = chooseRandom(set)
            }
            return randomElement
        }

        fun chooseRandom(set: Set<ActorPath>): ActorPath {
            return set.elementAt(Random().nextInt(set.size))
        }
    }
}