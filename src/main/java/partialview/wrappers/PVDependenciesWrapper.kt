package partialview.wrappers

import akka.actor.ActorRef
import akkanetwork.NodeID

class PVDependenciesWrapper(val contactNode: NodeID?,
                            val activeView: MutableSet<ActorRef> = mutableSetOf(),
                            val passiveView: MutableSet<ActorRef> = mutableSetOf(),
                            val passiveActiveView: MutableSet<ActorRef> = mutableSetOf(),
                            val myID: String,
                            val globalViewActor: ActorRef)