package partialview.wrappers

import akka.actor.ActorRef
import akkanetwork.NodeID
import partialview.PVMessagesCounter


class PVDependenciesWrapper(val contactNode: NodeID,
                            val myID: NodeID,
                            val activeView: MutableSet<ActorRef> = mutableSetOf(),
                            val passiveView: MutableSet<ActorRef> = mutableSetOf(),
                            val passiveActiveView: MutableSet<ActorRef> = mutableSetOf(),
                            val mCounter: PVMessagesCounter= PVMessagesCounter(),
                            var globalActorRef: ActorRef? = null)