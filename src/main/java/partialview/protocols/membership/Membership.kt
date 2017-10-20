package partialview.protocols.membership

import akka.actor.ActorRef
import akkanetwork.AkkaUtils
import partialview.PVHelpers
import partialview.ViewOperations
import partialview.protocols.crashrecovery.CrashRecovery
import partialview.protocols.crashrecovery.Priority
import partialview.protocols.membership.messages.DiscoverContactRefMessage
import partialview.protocols.membership.messages.ForwardJoinMessage

class Membership(private val activeView: MutableSet<ActorRef> = mutableSetOf(),
                 private val viewOperations: ViewOperations,
                 private val self: ActorRef,
                 private val crashRecovery: CrashRecovery) {

    fun join(sender: ActorRef) {
        sender.tell(DiscoverContactRefMessage(), self)
        viewOperations.addNodeActiveView(sender)
        // TODO: Global new node
        activeView.forEach {
            if (it != sender) {
                it.tell(ForwardJoinMessage(sender, PVHelpers.ARWL), self)
            }
        }
    }

    fun discoverContactRefMessage(sender: ActorRef) {
        viewOperations.addNodeActiveView(sender)
    }

    fun forwardJoin(timeToLive: Int, newNode: ActorRef, sender: ActorRef) {
        if (timeToLive == 0 || activeView.size == 1) {
            viewOperations.addNodeActiveView(newNode)
            newNode.tell(DiscoverContactRefMessage(), self)
        } else {
            if(timeToLive == PVHelpers.PRWL) {
                viewOperations.addNodePassiveView(newNode)
            }
            val randomNeighbor = AkkaUtils.chooseRandomWithout(sender, activeView)
            randomNeighbor?.tell(ForwardJoinMessage(newNode, timeToLive - 1), self)
        }
    }

    fun disconnect(sender: ActorRef) {
        if (activeView.contains(sender)){
            viewOperations.activeToPassive(sender)
            if(activeView.size == 0) { crashRecovery.sendNeighborRequest(Priority.HIGH) }
        }
    }
}