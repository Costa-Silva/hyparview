package partialview

import akka.actor.AbstractActor
import akka.actor.Props
import akka.actor.Terminated
import akkanetwork.AkkaConstants
import akkanetwork.AkkaConstants.Companion.GLOBAL_ACTOR
import akkanetwork.AkkaConstants.Companion.SYSTEM_NAME
import akkanetwork.AkkaUtils
import globalview.messages.external.PingMessage
import partialview.protocols.crashrecovery.messages.NeighborRequestMessage
import partialview.protocols.crashrecovery.messages.NeighborRequestReplyMessage
import partialview.protocols.entropy.messages.CutTheWireMessage
import partialview.protocols.entropy.messages.KillMessage
import partialview.protocols.gossip.messages.GossipMessage
import partialview.protocols.gossip.messages.StatusMessageWrapper
import partialview.protocols.membership.messages.DisconnectMessage
import partialview.protocols.membership.messages.DiscoverContactRefMessage
import partialview.protocols.membership.messages.ForwardJoinMessage
import partialview.protocols.membership.messages.JoinMessage
import partialview.protocols.suffle.messages.ShuffleMessage
import partialview.protocols.suffle.messages.ShuffleReplyMessage
import partialview.wrappers.PVDependenciesWrapper
import java.util.*

class PartialViewActor(pvWrapper: PVDependenciesWrapper): AbstractActor() {

    private val partialView: PartialView = PartialView(pvWrapper, context, self, AkkaUtils.lookUpRemote(context, SYSTEM_NAME, pvWrapper.myID, GLOBAL_ACTOR))

    companion object {
        fun props(pvWrapper: PVDependenciesWrapper): Props {
            return Props.create(PartialViewActor::class.java) { PartialViewActor(pvWrapper) }
        }
    }

    init {
        // Ignore when it's the contact node joining the system
        if(pvWrapper.contactNode != pvWrapper.myID) {
            val contactGlobal = object : TimerTask() {
                override fun run() {
                    if (pvWrapper.globalActorRef != null) {
                        val contactRemote = AkkaUtils.lookUpRemote(context, AkkaConstants.SYSTEM_NAME, pvWrapper.contactNode, AkkaConstants.PARTIAL_ACTOR)
                        pvWrapper.globalActorRef?.let { contactRemote.tell(JoinMessage(it), self)  }
                    } else {
                        System.err.println("Global was not ready yet")
                    }
                }
            }
            Timer().schedule(contactGlobal, 500)
        }
    }

    override fun createReceive(): Receive {
        return receiveBuilder()
                .match(Terminated::class.java) { partialView.crashed(it.actor) }
                .match(JoinMessage::class.java) { partialView.joinReceived(sender, it.newGlobalActor) }
                .match(DiscoverContactRefMessage::class.java) { partialView.discoverContactRefMessageReceived(sender) }
                .match(ForwardJoinMessage::class.java) { partialView.forwardJoinReceived(it.timeToLive, it.newNode, sender) }
                .match(DisconnectMessage::class.java) { partialView.disconnectReceived(sender) }
                .match(NeighborRequestMessage::class.java) { partialView.helpMeReceived(it.priority, sender) }
                .match(NeighborRequestReplyMessage::class.java) { partialView.helpMeResponseReceived(it.result, sender) }
                .match(ShuffleMessage::class.java) { partialView.shuffleReceived(it.sample, it.timeToLive, it.uuid, it.origin, sender) }
                .match(ShuffleReplyMessage::class.java) { partialView.shuffleReplyReceived(it.sample, it.uuid) }
                .match(PingMessage::class.java) { sender.tell(true, sender) }

                // Global View Messages
                .match(StatusMessageWrapper::class.java) { partialView.broadcast(it) }
                .match(GossipMessage::class.java) { partialView.gossipMessageReceived(it) }

                // Entropy Messages
                .match(CutTheWireMessage::class.java) { partialView.cutTheWireReceived(it.disconnectNodeID) }
                .match(KillMessage::class.java) { partialView.killReceived() }
                .build()
    }
}