package globalview

import akka.actor.ActorRef
import java.io.Serializable
import java.util.*

class GVSharedData(val eventList: List<Pair<UUID, Event>>,
                   val pendingEvents: MutableMap<UUID, Event>,
                   val toRemove: MutableSet<ActorRef>,
                   val globalView: MutableMap<ActorRef, ActorRef>,
                   val gVMCounter: GVMessagesCounter): Serializable