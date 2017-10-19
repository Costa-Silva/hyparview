package partialview

import akka.actor.ActorContext
import akka.actor.ActorRef
import akkanetwork.AkkaUtils
import partialview.protocols.membership.messages.DisconnectMessage

class ViewOperations(private var activeView: MutableSet<ActorRef>,
                     private var passiveView: MutableSet<ActorRef>,
                     private var self: ActorRef,
                     private var context: ActorContext) {

    var watchSet = mutableSetOf<ActorRef>()

    fun addNodeActiveView(node: ActorRef) {
        if(node != self && !activeView.contains(node)) {
            if(PVHelpers.activeViewisFull(activeView)) {
                dropRandomElementFromActiveView()
            }
            addToWatchSet(node)
            activeView.add(node)
        }
    }

    fun addNodePassiveView(node: ActorRef) {
        if(node != self && !activeView.contains(node) &&
                !passiveView.contains(node)) {
            if(PVHelpers.passiveViewisFull(passiveView)) {
                val actor = AkkaUtils.chooseRandom(passiveView)
                if (actor != null) {
                    passiveView.remove(actor)
                    removeFromWatchSet(actor)
                }
            }
            addToWatchSet(node)
            passiveView.add(node)
        }
    }

    fun passiveToActive(node: ActorRef) {
        passiveView.remove(node)
        addNodeActiveView(node)
    }

    fun activeToPassive(node: ActorRef) {
        activeView.remove(node)
        addNodePassiveView(node)
    }

    fun nodeFailedSoRemoveFromActive(node: ActorRef) {
        activeView.remove(node)
        removeFromWatchSet(node)
    }

    fun nodeFailedSoRemoveFromPassive(node: ActorRef?) {
        if (node != null ) {
            passiveView.remove(node)
            removeFromWatchSet(node)
        }
    }

    private fun dropRandomElementFromActiveView() {
        val node = AkkaUtils.chooseRandom(activeView)
        if (node != null) {
            node.tell(DisconnectMessage(), self)
            activeToPassive(node)
        }
    }

    private fun addToWatchSet(node: ActorRef) {
        if(!watchSet.contains(node)) {
            watchSet.add(node)
            context.watch(node)
        }
    }

    private fun removeFromWatchSet(node: ActorRef) {
        if(watchSet.contains(node)) {
            watchSet.remove(node)
            context.unwatch(node)
        }
    }
}