package partialview

import akka.actor.AbstractActor
import akka.actor.ActorPath
import akka.actor.Props
import akkanetwork.AkkaConstants
import akkanetwork.AkkaUtils
import akkanetwork.AkkaUtils.Companion.chooseRandomWithout
import akkanetwork.NodeID
import partialview.messages.DisconnectMessage
import partialview.messages.ForwardJoinMessage
import partialview.messages.JoinMessage

class PartialViewActor(contactNode: NodeID?, val fanout: Int,
                       val partialView: PartialView = PartialView()) : AbstractActor() {

    companion object {
        fun props(contactNode: NodeID?, fanout: Int): Props {
            return Props.create(PartialViewActor::class.java) { PartialViewActor(contactNode, fanout)}
        }
    }

    init {
        // Ignore when it's the contact node joining the system
        if(contactNode != null) {
            val contactRemote = AkkaUtils.lookUpRemote(context, AkkaConstants.SYSTEM_NAME, contactNode, contactNode.identifier)
            contactRemote.tell(JoinMessage(), self)
        }
    }

    override fun createReceive(): Receive {
        return receiveBuilder()
                .match(JoinMessage::class.java) { JoinReceived() }
                .match(ForwardJoinMessage::class.java) { forwardJoinReceived(it.timeToLive, it.newNode) }
                .match(DisconnectMessage::class.java) { deleteReceived()}
                .build()
    }

    private fun JoinReceived() {
        partialView.addNodeActiveView(sender.path(), self, context)
        // TODO: Global new node
        partialView.activeView.forEach {
            if (it != sender.path()) {
                val actor = context.actorSelection(it)
                actor.tell(ForwardJoinMessage(sender.path(), PVHelpers.ARWL), self)
            }
        }
    }

    private fun forwardJoinReceived(timeToLive: Int, newNode: ActorPath) {
        if (timeToLive == 0 || partialView.activeView.size == 1) {
            partialView.addNodeActiveView(newNode, self, context)
        } else {
            if(timeToLive == PVHelpers.PRWL) {
                partialView.addNodePassiveView(newNode, self)
            }
            val randomNeighbor = chooseRandomWithout(sender.path(), partialView.activeView)
            val actor = context.actorSelection(randomNeighbor)
            actor.tell(ForwardJoinMessage(newNode, timeToLive - 1), self)
        }
    }

    private fun deleteReceived() {
        val nodePath = sender.path()
        if (partialView.activeView.contains(nodePath)){
            partialView.activeView.remove(nodePath)
            partialView.addNodePassiveView(nodePath, self)
        }
    }
}