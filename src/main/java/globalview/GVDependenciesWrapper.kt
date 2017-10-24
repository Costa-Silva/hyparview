package globalview

import akka.actor.ActorRef
import akkanetwork.NodeID
import java.util.*

class GVDependenciesWrapper(val eventList: LinkedList<UUID> = LinkedList(),
                            val pendingEvents: MutableMap<UUID, Event> = mutableMapOf(),
                            val toRemove: MutableSet<ActorRef> = mutableSetOf(),
                            val globalView: MutableMap<ActorRef, ActorRef> = mutableMapOf(),
                            val nodeId: NodeID,
                            val imContact: Boolean,
                            val partialActor: ActorRef)