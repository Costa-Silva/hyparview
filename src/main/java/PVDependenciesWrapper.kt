import akka.actor.ActorRef
import akkanetwork.NodeID

class PVDependenciesWrapper(val contactNode: NodeID?, val fanout: Int,
                            val activeView: MutableSet<ActorRef> = mutableSetOf(),
                            val passiveView: MutableSet<ActorRef> = mutableSetOf())