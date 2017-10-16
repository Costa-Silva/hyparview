package partialview

import akka.actor.AbstractActor
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
        val contact = AkkaUtils.lookUpRemote(context, AkkaConstants.SYSTEM_NAME, contactNode,
                AkkaConstants.CONTACT_NODE)

        contact.tell(JoinMessage(), self)
    }

    override fun createReceive(): Receive {
        return receiveBuilder()
                .match(JoinMessage::class.java) { _ ->
                    partialView.activeView.add(sender.path())
                    // comNewNode
                    partialView.activeView.forEach {
                        if (it != sender.path()) {
                            val actor = context.actorSelection(it)
                            actor.tell(ForwardJoinMessage(sender.path(), PVHelpers.ARWL), self)
                        }
                    }
                }.match(ForwardJoinMessage::class.java) { message ->
                if (message.timeToLive == 0 || partialView.activeView.size == 1) {
                    partialView.activeView.add(message.newNode)
                } else {
                    if(message.timeToLive == PVHelpers.PRWL) {
                        partialView.passiveView.add(message.newNode)
                    }
                    val randomNeighbor = PVHelpers.chooseRandom(partialView.activeView, sender.path())
                    val actor = context.actorSelection(randomNeighbor)
                    actor.tell(ForwardJoinMessage(message.newNode, message.timeToLive - 1), self)
                }
        }.build()
    }
}