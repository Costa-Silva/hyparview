package partialview.protocols.membership

import akka.actor.ActorRef
import akkanetwork.AkkaUtils
import globalview.messages.internal.PartialDiscoveredNewNode
import partialview.PVHelpers
import partialview.PVMessagesCounter
import partialview.ViewOperations
import partialview.protocols.crashrecovery.CrashRecovery
import partialview.protocols.crashrecovery.Priority
import partialview.protocols.membership.messages.DiscoverContactRefMessage
import partialview.protocols.membership.messages.ForwardJoinMessage

class Membership(private val activeView: MutableSet<ActorRef> = mutableSetOf(),
                 private val viewOperations: ViewOperations,
                 private val self: ActorRef,
                 private val crashRecovery: CrashRecovery,
                 private val gvActor: ActorRef,
                 private val mCounter: PVMessagesCounter) {

    fun join(sender: ActorRef, newGlobalActor: ActorRef) {
        mCounter.joinsReceived++
        sender.tell(DiscoverContactRefMessage(), self)
        gvActor.tell(PartialDiscoveredNewNode(newGlobalActor), self)
        viewOperations.addNodeActiveView(sender)
        activeView.forEach {
            if (it != sender) {
                mCounter.forwardJoinsSent++
                it.tell(ForwardJoinMessage(sender, PVHelpers.ARWL), self)
            }
        }
    }

    fun discoverContactRefMessage(sender: ActorRef) {
        //gvActor.tell(PartialDiscoveredNewNode(newGlobalActor), self)
        viewOperations.addNodeActiveView(sender)
    }

    fun forwardJoin(timeToLive: Int, newNode: ActorRef, sender: ActorRef) {
        mCounter.forwardJoinsReceived++
        if (timeToLive == 0 || activeView.size == 1) {
            viewOperations.addNodeActiveView(newNode)
            newNode.tell(DiscoverContactRefMessage(), self)
        } else {
            if(timeToLive == PVHelpers.PRWL) {
                viewOperations.addNodePassiveView(newNode)
            }
            val randomNeighbor = AkkaUtils.chooseRandomWithout(sender, activeView)
            randomNeighbor?.let {
                mCounter.forwardJoinsSent++
                it.tell(ForwardJoinMessage(newNode, timeToLive - 1), self)
            }
        }
    }

    fun disconnect(sender: ActorRef) {
        mCounter.disconnectsReceived++
        if (activeView.contains(sender)){
            viewOperations.activeToPassive(sender)
            if(activeView.size == 0) { crashRecovery.sendNeighborRequest(Priority.HIGH) }
        }
    }
}