package partialview
import akka.actor.ActorPath
import akkanetwork.NodeID

data class PartialView(var activeView: MutableMap<NodeID, ActorPath> = mutableMapOf(),
                       var passiveView: MutableMap<NodeID, ActorPath> = mutableMapOf())