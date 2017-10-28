package globalview

import akka.actor.AbstractActor
import akka.actor.Props
import communicationview.messages.StatusMessageWrapper
import globalview.messages.external.ConflictMessage
import globalview.messages.external.GiveGlobalMessage
import globalview.messages.external.GlobalMessage
import globalview.messages.external.PingMessage
import globalview.messages.internal.MayBeDeadMessage
import globalview.messages.internal.PartialDiscoveredNewNode

class GlobalViewActor(gvWrapper: GVDependenciesWrapper): AbstractActor() {

    private val globalView = GlobalView(gvWrapper.eventList, gvWrapper.pendingEvents, gvWrapper.toRemove,
            gvWrapper.globalView, self, gvWrapper.system, gvWrapper.gVMCounter, gvWrapper.partialActor,gvWrapper.commActor,
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

                // From myself
                .match(String::class.java) {
                    when(it) {
                        GVHelpers.SEND_HASH_MESSAGE -> globalView.sendHash()
                        GVHelpers.SEND_EVENTS_MESSAGE -> globalView.sendEvents()
                    }
                }

                // Internal messages
                // from Partial view
                .match(MayBeDeadMessage::class.java) { globalView.partialNodeMayBeDead(it.partialNode) }
                .match(PartialDiscoveredNewNode::class.java) { globalView.globalNewNode(it.newGlobalNode, it.newPartialNode ,true) }

                // from Communication view
                .match(StatusMessageWrapper::class.java) { globalView.partialDeliver(it) }

                .build()

    }
}