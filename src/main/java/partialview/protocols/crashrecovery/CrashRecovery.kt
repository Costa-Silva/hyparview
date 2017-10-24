package partialview.protocols.crashrecovery

import akka.actor.ActorRef
import akkanetwork.AkkaUtils
import globalview.messages.internal.MayBeDeadMessage
import partialview.PVHelpers
import partialview.PVMessagesCounter
import partialview.ViewOperations
import partialview.protocols.crashrecovery.messages.NeighborRequestMessage
import partialview.protocols.crashrecovery.messages.NeighborRequestReplyMessage

class CrashRecovery(private val activeView: MutableSet<ActorRef>,
                    private val passiveActiveView: MutableSet<ActorRef>,
                    private val self: ActorRef,
                    private val viewOperations: ViewOperations,
                    private val gvActor: ActorRef,
                    private val mCounter: PVMessagesCounter) {

    private val ongoingNeighborRequests = mutableSetOf<ActorRef>()

    fun crashed(node: ActorRef) {
        if(activeView.contains(node)) {
            viewOperations.nodeFailedSoRemoveFromActive(node)
            val priority = if(activeView.size == 0) Priority.HIGH else Priority.LOW
            sendNeighborRequest(priority)
            // TODO
            gvActor.tell(MayBeDeadMessage(node),self)
        } else if(passiveActiveView.contains(node)) {
            viewOperations.removeFromPassiveActive(node)
        }
    }

    fun sendNeighborRequest(priority: Priority) {
        val actor = AkkaUtils.chooseRandomWithout(ongoingNeighborRequests, passiveActiveView)
        actor?.let {
            mCounter.neighborRequestsSent++
            ongoingNeighborRequests.add(it)
            it.tell(NeighborRequestMessage(priority), self)
        }
    }

    fun neighborRequest(priority: Priority, sender: ActorRef) {
        mCounter.neighborRequestsReceived++
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
            val actor = AkkaUtils.chooseRandomWithout(ongoingNeighborRequests, passiveActiveView)
            actor?.let {
                val priority = if(activeView.size == 0) Priority.HIGH else Priority.LOW
                mCounter.neighborRequestsSent++
                ongoingNeighborRequests.add(it)
                it.tell(NeighborRequestMessage(priority), self)
            }
        }
        ongoingNeighborRequests.remove(sender)
    }
}