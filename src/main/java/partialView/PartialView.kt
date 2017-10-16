package partialView
import akkaNetwork.NodeID

data class PartialView(var activeView: MutableSet<NodeID> = mutableSetOf(),
                       var passiveView: MutableSet<NodeID> = mutableSetOf()) {
}