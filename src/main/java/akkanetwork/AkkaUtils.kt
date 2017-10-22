package akkanetwork

import akka.actor.ActorContext
import akka.actor.ActorRef
import akka.actor.ActorSelection
import com.typesafe.config.ConfigFactory
import java.util.*

class AkkaUtils {
    companion object {

        fun numberFromIdentifier(id: String): Int {
            return Integer.parseInt(id.split("node")[0])
        }

        fun lookUpRemote(context: ActorContext, systemName: String, ip: NodeID, type: String): ActorSelection {
            return context.actorSelection("akka.tcp://$systemName@$ip/user/${ip.identifier+type}")
        }

        fun createNodeID(nodeID: String?): NodeID? {
            val config = ConfigFactory.load()
            return try {
                nodeID?.let {
                    val ip = config.getString("nodes.ip.$nodeID")
                    val port = config.getString("nodes.port.$nodeID")
                    NodeID(ip, port, nodeID)
                }
            } catch (e: Exception) {
                System.err.println("BAD NODEID")
                null
            }
        }

        fun chooseRandomWithout(values: Set<ActorRef>, set: Set<ActorRef>): ActorRef? {
            if(set.size == values.size && set.containsAll(values) || (set.size < values.size && values.containsAll(set))
                    || set.isEmpty()) {
                return null
            }

            var randomElement = chooseRandom(set)
            while (values.contains(randomElement)) {
                randomElement = chooseRandom(set)
            }
            return randomElement
        }

        fun chooseRandomWithout(withoutElement: ActorRef, set: Set<ActorRef>): ActorRef? {
            if(set.size == 1 && set.contains(withoutElement)) {
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