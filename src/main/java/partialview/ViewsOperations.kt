package partialview

import akka.actor.ActorRef
import akkanetwork.AkkaUtils
import partialview.messages.DisconnectMessage

class ViewsOperations(private var activeView: MutableSet<ActorRef>,
                      private var passiveView: MutableSet<ActorRef>,
                      private var self: ActorRef) {


    fun addNodeActiveView(node: ActorRef) {
        if(node != self && !activeView.contains(node)) {
            if(PVHelpers.activeViewisFull(activeView)) {
                dropRandomElementFromActiveView()
            }
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
        activeView.remove(node)
        addNodePassiveView(node)
    }

    private fun dropRandomElementFromActiveView() {
        val node = AkkaUtils.chooseRandom(activeView)
        node.tell(DisconnectMessage(), self)
        activeView.remove(node)
        passiveView.add(node)
    }
}