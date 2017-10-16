package partialview

import akka.actor.AbstractActor
import akka.actor.ActorPath
import akka.actor.Props
import akkanetwork.AkkaConstants
import akkanetwork.AkkaUtils
import akkanetwork.NodeID
import partialview.messages.ForwardJoinMessage
import partialview.messages.JoinMessage

class PartialViewActor(contactNode: NodeID, val fanout: Int,
                       val partialView: PartialView = PartialView()) : AbstractActor() {

    companion object {
        fun props(contactNode: NodeID, fanout: Int): Props {
            return Props.create(PartialViewActor::class.java) { PartialViewActor(contactNode, fanout)}
        }
    }

    init {
        val contact = AkkaUtils.lookUpRemote(context, AkkaConstants.SYSTEM_NAME, contactNode, AkkaConstants.CONTACT_NODE)
        contact.tell(JoinMessage(), self)
    }

    override fun createReceive(): Receive {
        return receiveBuilder()
                .match(JoinMessage::class.java) { JoinReceived() }
                .match(ForwardJoinMessage::class.java) { forwardJoinReceived(it.timeToLive, it.newNode) }
                .build()
    }

    fun JoinReceived() {
        partialView.activeView.add(sender.path())
        // comNewNode
        partialView.activeView.forEach {
            if (it != sender.path()) {
                val actor = context.actorSelection(it)
                actor.tell(ForwardJoinMessage(sender.path(), PVHelpers.ARWL), self)
            }
        }
    }

    fun forwardJoinReceived(timeToLive: Int, newNode: ActorPath) {
        if (timeToLive == 0 || partialView.activeView.size == 1) {
            partialView.activeView.add(newNode)
        } else {
            if(timeToLive == PVHelpers.PRWL) {
                partialView.passiveView.add(newNode)
            }
            val randomNeighbor = PVHelpers.chooseRandom(partialView.activeView, sender.path())
            val actor = context.actorSelection(randomNeighbor)
            actor.tell(ForwardJoinMessage(newNode, timeToLive - 1), self)
        }
    }
}