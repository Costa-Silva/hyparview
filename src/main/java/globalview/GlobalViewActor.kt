package globalview

import akka.actor.AbstractActor
import akka.actor.Props
import akkanetwork.AkkaConstants
import akkanetwork.AkkaConstants.Companion.PARTIAL_ACTOR
import akkanetwork.AkkaUtils
import globalview.messages.external.ConflictMessage
import globalview.messages.external.GiveGlobalMessage
import globalview.messages.external.GlobalMessage
import globalview.messages.external.PingMessage
import globalview.messages.internal.MayBeDeadMessage
import globalview.messages.internal.PartialDiscoveredNewNode
import partialview.protocols.gossip.messages.StatusMessageWrapper

class GlobalViewActor(gvWrapper: GVDependenciesWrapper): AbstractActor() {

    private val globalView = GlobalView(gvWrapper.eventList, gvWrapper.pendingEvents, gvWrapper.toRemove,
            gvWrapper.globalView, self, AkkaUtils.lookUpRemote(context, AkkaConstants.SYSTEM_NAME,
            gvWrapper.nodeId, PARTIAL_ACTOR), gvWrapper.imContact)

    companion object {
        fun props(gvWrapper: GVDependenciesWrapper): Props {
            return Props.create(GlobalViewActor::class.java) { GlobalViewActor(gvWrapper) }
        }
    }

    override fun createReceive(): Receive {
        return receiveBuilder()
                .match(GlobalMessage::class.java) { globalView.receivedGlobalMessage(it.globalView, it.eventList)}
                .match(GiveGlobalMessage::class.java) { globalView.giveGlobalReceived(sender) }
                .match(ConflictMessage::class.java) { globalView.conflictMessageReceived(it.myGlobalView) }
                .match(PingMessage::class.java) { sender.tell(true, sender) }

                // Internal messages
                .match(StatusMessageWrapper::class.java) { globalView.partialDeliver(it) }
                .match(MayBeDeadMessage::class.java) { globalView.globalMayBeDead(it.node) }
                .match(PartialDiscoveredNewNode::class.java) { globalView.globalNewNode(it.newGlobalNode, true) }
                .build()

    }
}