package partialview

import akka.actor.AbstractActor
import akka.actor.Props
import akka.actor.Terminated
import akkanetwork.AkkaConstants
import akkanetwork.AkkaUtils
import akkanetwork.NodeID
import partialview.protocols.crashrecovery.messages.HelpMeMessage
import partialview.protocols.crashrecovery.messages.HelpMeReplyMessage
import partialview.protocols.membership.messages.*
import partialview.protocols.suffle.messages.ShuffleMessage
import partialview.protocols.suffle.messages.ShuffleReplyMessage

class PartialViewActor(contactNode: NodeID?, val fanout: Int) : AbstractActor() {

    private val partialView: PartialView = PartialView(context= context, self = self)

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
                .match(Terminated::class.java) { partialView.crashed(it.actor) }
                .match(JoinMessage::class.java) { partialView.joinReceived(sender) }
                .match(DiscoverContactRefMessage::class.java) { partialView.discoverContactRefMessageReceived(sender) }
                .match(ForwardJoinMessage::class.java) { partialView.forwardJoinReceived(it.timeToLive, it.newNode, sender) }
                .match(DisconnectMessage::class.java) { partialView.disconnectReceived(sender)}
                .match(BroadcastMessage::class.java) { partialView.broadcastReceived(it, sender) }
                .match(HelpMeMessage::class.java) { partialView.helpMeReceived(it.priority, sender) }
                .match(HelpMeReplyMessage::class.java) { partialView.helpMeResponseReceived(it.result, sender) }
                .match(ShuffleMessage::class.java) { partialView.shuffleReceived(it.sample, it.timeToLive, it.uuid, it.origin, sender) }
                .match(ShuffleReplyMessage::class.java) { partialView.shuffleReplyReceived(it.sample, it.uuid) }
                .build()
    }

    fun broadcast(message: BroadcastMessage) { partialView.broadcast(message) }
}