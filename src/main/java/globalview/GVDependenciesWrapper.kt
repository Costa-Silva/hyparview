package globalview

import akka.actor.ActorRef
import akka.actor.ActorSystem
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class GVDependenciesWrapper(val eventList: LinkedList<Pair<UUID, Event>> = LinkedList(),
                            val pendingEvents: ConcurrentMap<UUID, Event> = ConcurrentHashMap<UUID, Event>(),
                            val toRemove: MutableSet<ActorRef> = mutableSetOf(),
                            val globalView: ConcurrentMap<ActorRef, ActorRef> = ConcurrentHashMap<ActorRef, ActorRef>(),
                            val imContact: Boolean,
                            val partialActor: ActorRef,
                            val commActor: ActorRef,
                            val system: ActorSystem,
                            val gVMCounter: GVMessagesCounter,
                            val testActivated: Boolean,
                            val myID: String)