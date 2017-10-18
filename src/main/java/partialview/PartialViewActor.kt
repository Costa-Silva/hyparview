package partialview

import akka.actor.AbstractActor
import akka.actor.Props
import akkanetwork.AkkaConstants
import akkanetwork.AkkaUtils
import akkanetwork.NodeID
import partialview.crashrecoveryprotocol.messages.HelpMeMessage
import partialview.crashrecoveryprotocol.messages.HelpMeResponseMessage
import partialview.messages.*

class PartialViewActor(contactNode: NodeID?, val fanout: Int) : AbstractActor() {

    private val partialView: PartialView = PartialView(self = self)

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
                .match(JoinMessage::class.java) { partialView.JoinReceived(sender) }
                .match(DiscoverContactRefMessage::class.java) { partialView.DiscoverContactRefMessageReceived(sender) }
                .match(ForwardJoinMessage::class.java) { partialView.forwardJoinReceived(it.timeToLive, it.newNode, sender) }
                .match(DisconnectMessage::class.java) { partialView.disconnectReceived(sender)}
                .match(BroadcastMessage::class.java) { partialView.broadcastReceived(it, sender) }
                .match(HelpMeMessage::class.java) { partialView.helpMeReceived(it.requestUUID, it.priority, sender) }
                .match(HelpMeResponseMessage::class.java) { partialView.helpMeResponseReceived(it.requestUUID, it.result, sender)}
                .build()
    }

    fun broadcast(message: BroadcastMessage) { partialView.broadcast(message) }
}