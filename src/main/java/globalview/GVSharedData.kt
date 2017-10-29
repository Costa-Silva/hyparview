package globalview

import akka.actor.ActorRef
import communicationview.wrappers.CommunicationWrapper
import java.util.*

class GVSharedData(val eventList: LinkedList<Pair<UUID, Event>>,
                   val pendingEvents: MutableMap<UUID, Event>,
                   val toRemove: MutableSet<ActorRef>,
                   val globalView: MutableMap<ActorRef, ActorRef>,
                   val gVMCounter: GVMessagesCounter,
                   val commWrapper: CommunicationWrapper)