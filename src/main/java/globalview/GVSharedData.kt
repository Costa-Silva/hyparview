package globalview

import akka.actor.ActorRef
import java.util.*

class GVSharedData(val eventList: LinkedList<UUID>,
                   val pendingEvents: MutableMap<UUID, Event>,
                   val toRemove: MutableSet<ActorRef>,
                   val globalView: MutableMap<ActorRef, ActorRef>)