package globalview

import akka.actor.AbstractActor
import akka.actor.Props
import globalview.GVHelpers.Companion.SEND_EVENTS_MESSAGE
import globalview.GVHelpers.Companion.SEND_HASH_MESSAGE
import globalview.messages.external.ConflictMessage
import globalview.messages.external.GiveGlobalMessage
import globalview.messages.external.GlobalMessage
import globalview.messages.external.PingMessage
import globalview.messages.internal.MayBeDeadMessage
import globalview.messages.internal.PartialDiscoveredNewNode
import partialview.protocols.gossip.messages.StatusMessageWrapper

class GlobalViewActor(gvWrapper: GVDependenciesWrapper): AbstractActor() {

    private val globalView = GlobalView(gvWrapper.eventList, gvWrapper.pendingEvents, gvWrapper.toRemove,
            gvWrapper.globalView, self, gvWrapper.partialActor , gvWrapper.system, gvWrapper.gVMCounter,
            gvWrapper.imContact)

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
                .match(MayBeDeadMessage::class.java) { globalView.partialNodeMayBeDead(it.partialNode) }
                .match(PartialDiscoveredNewNode::class.java) { globalView.globalNewNode(it.newGlobalNode, it.newPartialNode ,true) }

                // From myself
                .match(String::class.java) {
                    when(it) {
                        SEND_HASH_MESSAGE -> globalView.sendHash()
                        SEND_EVENTS_MESSAGE -> globalView.sendEvents()
                    }
                }
                .build()

    }
}