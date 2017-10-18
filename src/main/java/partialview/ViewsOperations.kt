package partialview

import akka.actor.ActorContext
import akka.actor.ActorRef
import akkanetwork.AkkaUtils
import partialview.messages.DisconnectMessage

class ViewsOperations(private var activeView: MutableSet<ActorRef>,
                      private var passiveView: MutableSet<ActorRef>,
                      private var self: ActorRef,
                      private var context: ActorContext) {


    fun addNodeActiveView(node: ActorRef) {
        if(node != self && !activeView.contains(node)) {
            if(PVHelpers.activeViewisFull(activeView)) {
                dropRandomElementFromActiveView()
            }
            context.watch(node)
            activeView.add(node)
        }
    }
    fun addNodePassiveView(node: ActorRef) {
        if(node != self && !activeView.contains(node) &&
                !passiveView.contains(node)) {
            if(PVHelpers.passiveViewisFull(passiveView)) {
                passiveView.remove(AkkaUtils.chooseRandom(passiveView))
            }
            passiveView.add(node)
        }
    }


    fun passiveToActive(node: ActorRef) {
        passiveView.remove(node)
        addNodeActiveView(node)
    }

    fun activeToPassive(node: ActorRef) {
        removeFromActive(node)
        addNodePassiveView(node)
    }

    fun removeFromActive(node: ActorRef) {
        activeView.remove(node)
        context.unwatch(node)
    }
    private fun dropRandomElementFromActiveView() {
        val node = AkkaUtils.chooseRandom(activeView)
        node.tell(DisconnectMessage(), self)
        activeToPassive(node)
    }
}