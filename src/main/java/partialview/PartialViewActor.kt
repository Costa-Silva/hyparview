package partialview

import akka.actor.AbstractActor
import akka.actor.Props
import akkanetwork.AkkaConstants
import akkanetwork.AkkaUtils
import akkanetwork.NodeID

class PartialViewActor(val myID: NodeID,val contactNode: NodeID, val fanout: Int,
                       val partialView: PartialView = PartialView()) : AbstractActor() {

    companion object {
        fun props(myID: NodeID, contactNode: NodeID, fanout: Int): Props {
            return Props.create(PartialViewActor::class.java) { PartialViewActor(myID, contactNode, fanout)}
        }
    }

    init {
        val contact = AkkaUtils.lookUpRemote(context, AkkaConstants.SYSTEM_NAME, contactNode,
                AkkaConstants.CONTACT_NODE)

        contact.tell(JoinMessage(myID), self)
    }

    override fun createReceive(): Receive {
        return receiveBuilder()
                .match(JoinMessage::class.java) { message ->
                    partialView.activeView.put(message.newNode, sender.path())
                    // comNewNode
                    partialView.activeView.filter { it.component1() != message.newNode }
                            .forEach {
                                val actor = context.actorSelection(it.value)
                                //actor.tell(,,)
                            }
                }.build()
    }
}