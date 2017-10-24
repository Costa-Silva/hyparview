package globalview

import akka.actor.ActorRef
import akkanetwork.NodeID
import java.util.*

class GVDependenciesWrapper(val eventList: LinkedList<UUID> = LinkedList(),
                            val pendingEvents: MutableMap<UUID, Event> = mutableMapOf(),
                            val toRemove: MutableSet<ActorRef> = mutableSetOf(),
                            val globalView: MutableSet<ActorRef> = mutableSetOf(),
                            val nodeId: NodeID,
                            val imContact: Boolean)