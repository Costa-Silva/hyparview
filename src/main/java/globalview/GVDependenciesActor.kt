package globalview

import akka.actor.ActorRef
import java.util.*

class GVDependenciesActor(val eventList: LinkedList<UUID>,
                          val pendingEvents: MutableMap<UUID, Event>,
                          val toRemove: MutableSet<ActorRef>,
                          val globalView: MutableSet<ActorRef>,
                          val pvActor: ActorRef)