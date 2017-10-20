package partialview.protocols.crashrecovery

import akka.actor.ActorRef
import akkanetwork.AkkaUtils
import partialview.PVHelpers
import partialview.ViewOperations
import partialview.protocols.crashrecovery.messages.NeighborRequestMessage
import partialview.protocols.crashrecovery.messages.NeighborRequestReplyMessage

class CrashRecovery(private var activeView: MutableSet<ActorRef>,
                    private var passiveView: MutableSet<ActorRef>,
                    private var self: ActorRef,
                    private var viewOperations: ViewOperations) {

    var deadNodesFromPassive = mutableSetOf<ActorRef>()
    var ongoingNeighborRequests = mutableSetOf<ActorRef>()

    fun crashed(node: ActorRef) {
        if(activeView.contains(node)) {
            viewOperations.nodeFailedSoRemoveFromActive(node)
            val priority = if(activeView.size == 0) Priority.HIGH else Priority.LOW
            sendNeighborRequest(priority)
        } else {
            deadNodesFromPassive.add(node)
        }
    }

    fun sendNeighborRequest(priority: Priority) {
        var actor = AkkaUtils.chooseRandomWithout(ongoingNeighborRequests, passiveView)

        while(deadNodesFromPassive.contains(actor)) {
            deadNodesFromPassive.remove(actor)
            viewOperations.nodeFailedSoRemoveFromPassive(actor)
            actor = AkkaUtils.chooseRandomWithout(ongoingNeighborRequests, passiveView)
        }
        if (actor != null) {
            ongoingNeighborRequests.add(actor)
            actor.tell(NeighborRequestMessage(priority), self)
        }
    }

    fun neighborRequest(priority: Priority, sender: ActorRef) {
        var result = NeighborRequestResult.DECLINED
            if(priority == Priority.HIGH || !PVHelpers.activeViewisFull(activeView)) {
                viewOperations.passiveToActive(sender)
                result = NeighborRequestResult.ACCEPTED
            }
            sender.tell(NeighborRequestReplyMessage(result), self)
    }

    fun neighborRequestReply(result: NeighborRequestResult, sender: ActorRef) {
        if (result == NeighborRequestResult.ACCEPTED) {
            viewOperations.passiveToActive(sender)
        } else {
            val actor = AkkaUtils.chooseRandomWithout(ongoingNeighborRequests, passiveView)
            actor?.let {
                val priority = if(activeView.size == 0) Priority.HIGH else Priority.LOW
                ongoingNeighborRequests.add(it)
                it.tell(NeighborRequestMessage(priority), self)
            }
        }
        ongoingNeighborRequests.remove(sender)
    }
}