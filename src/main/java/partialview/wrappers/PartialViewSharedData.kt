package partialview.wrappers

import akka.actor.ActorRef
import akkanetwork.NodeID
import java.io.Serializable

class PartialViewSharedData(val identifier: String, val contactNode: NodeID?, val activeView: MutableSet<ActorRef>,
                            val passiveView: MutableSet<ActorRef>,
                            val passiveActiveView: MutableSet<ActorRef>): Serializable