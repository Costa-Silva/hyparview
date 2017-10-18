package partialview
import akka.actor.ActorRef
import akkanetwork.AkkaUtils
import partialview.messages.DisconnectMessage
import partialview.messages.ForwardJoinMessage

data class PartialView(private var activeView: MutableSet<ActorRef> = mutableSetOf(),
                       private var passiveView: MutableSet<ActorRef> = mutableSetOf(),
                       private var self: ActorRef){


    fun JoinReceived(sender: ActorRef) {
        addNodeActiveView(sender)
        // TODO: Global new node
        activeView.forEach {
            if (it != sender) {
                it.tell(ForwardJoinMessage(sender, PVHelpers.ARWL), self)
            }
        }
    }

    fun forwardJoinReceived(timeToLive: Int, newNode: ActorRef, sender: ActorRef) {
        if (timeToLive == 0 || activeView.size == 1) {
            addNodeActiveView(newNode)
        } else {
            if(timeToLive == PVHelpers.PRWL) {
                addNodePassiveView(newNode)
            }
            val randomNeighbor = AkkaUtils.chooseRandomWithout(sender, activeView)
            randomNeighbor.tell(ForwardJoinMessage(newNode, timeToLive - 1), self)
        }
    }

    fun disconnectReceived(sender: ActorRef) {
        if (activeView.contains(sender)){
            activeView.remove(sender)
            addNodePassiveView(sender)
        }
    }


    fun addNodeActiveView(node: ActorRef) {
        if(node != self.path() && !activeView.contains(node)) {
            if(PVHelpers.activeViewisFull(activeView)) {
                dropRandomElementFromActiveView()
            }
            activeView.add(node)
        }
    }
    fun addNodePassiveView(node: ActorRef) {
        if(node != self.path() && !activeView.contains(node) &&
                !passiveView.contains(node)) {
            if(PVHelpers.passiveViewisFull(passiveView)) {
                passiveView.remove(AkkaUtils.chooseRandom(passiveView))
            }
            passiveView.add(node)
        }
    }

    private fun dropRandomElementFromActiveView() {
        val node = AkkaUtils.chooseRandom(activeView)
        node.tell(DisconnectMessage(), self)
        activeView.remove(node)
        passiveView.add(node)
    }
}