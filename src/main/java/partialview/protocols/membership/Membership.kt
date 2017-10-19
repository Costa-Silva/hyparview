package partialview.protocols.membership

import akka.actor.ActorRef
import akkanetwork.AkkaUtils
import partialview.PVHelpers
import partialview.ViewOperations
import partialview.protocols.membership.messages.DiscoverContactRefMessage
import partialview.protocols.membership.messages.ForwardJoinMessage

class Membership(private var activeView: MutableSet<ActorRef> = mutableSetOf(),
                 private var viewOperations: ViewOperations,
                 private var self: ActorRef) {

    fun joinReceived(sender: ActorRef) {
        sender.tell(DiscoverContactRefMessage(), self)
        viewOperations.addNodeActiveView(sender)
        // TODO: Global new node
        activeView.forEach {
            if (it != sender) {
                it.tell(ForwardJoinMessage(sender, PVHelpers.ARWL), self)
            }
        }
    }

    fun discoverContactRefMessageReceived(sender: ActorRef) {
        viewOperations.addNodeActiveView(sender)
    }

    fun forwardJoinReceived(timeToLive: Int, newNode: ActorRef, sender: ActorRef) {
        if (timeToLive == 0 || activeView.size == 1) {
            viewOperations.addNodeActiveView(newNode)
        } else {
            if(timeToLive == PVHelpers.PRWL) {
                viewOperations.addNodePassiveView(newNode)
            }
            val randomNeighbor = AkkaUtils.chooseRandomWithout(sender, activeView)
            randomNeighbor?.tell(ForwardJoinMessage(newNode, timeToLive - 1), self)
        }
    }

    fun disconnectReceived(sender: ActorRef) {
        if (activeView.contains(sender)){
            viewOperations.activeToPassive(sender)
        }
    }
}