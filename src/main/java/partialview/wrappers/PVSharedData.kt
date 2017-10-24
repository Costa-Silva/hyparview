package partialview.wrappers

import akka.actor.ActorRef
import akkanetwork.NodeID
import partialview.PVMessagesCounter
import java.io.Serializable

class PVSharedData(val identifier: String, val contactNode: NodeID?,
                   val activeView: MutableSet<ActorRef>,
                   val passiveView: MutableSet<ActorRef>,
                   val passiveActiveView: MutableSet<ActorRef>,
                   val mCounter: PVMessagesCounter): Serializable