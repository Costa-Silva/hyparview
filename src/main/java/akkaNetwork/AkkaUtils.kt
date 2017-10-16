package akkaNetwork

import akka.actor.ActorContext
import akka.actor.ActorSelection

class AkkaUtils {
    companion object {
        fun lookUpRemote(context: ActorContext, systemName: String, ip: NodeID, NodeName: String): ActorSelection {
            return context.actorSelection("akka.tcp://$systemName@$ip/user/$NodeName")
        }
    }
}