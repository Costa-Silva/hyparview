package globalview

import akka.actor.AbstractActor
import akka.actor.Props
import akkanetwork.AkkaConstants
import akkanetwork.AkkaUtils
import globalview.messages.ConflictMessage
import globalview.messages.GiveGlobalMessage
import globalview.messages.GlobalMessage
import globalview.messages.PingMessage
import partialview.protocols.gossip.messages.StatusMessageWrapper

class GlobalViewActor(gvWrapper: GVDependenciesWrapper): AbstractActor() {

    private val globalView = GlobalView(gvWrapper.eventList, gvWrapper.pendingEvents, gvWrapper.toRemove,
            gvWrapper.globalView, self, AkkaUtils.lookUpRemote(context, AkkaConstants.SYSTEM_NAME, gvWrapper.nodeId))

    companion object {
        fun props(gvWrapper: GVDependenciesWrapper): Props {
            return Props.create(GlobalViewActor::class.java) { GlobalViewActor(gvWrapper) }
        }
    }

    override fun createReceive(): Receive {
        return receiveBuilder()
                .match(StatusMessageWrapper::class.java) { globalView.partialDeliver(it) }
                .match(GlobalMessage::class.java) { globalView.receivedGlobalMessage(it.globalView, it.eventList)}
                .match(GiveGlobalMessage::class.java) { globalView.giveGlobalReceived(sender) }
                .match(ConflictMessage::class.java) { globalView.conflictMessageReceived(it.myGlobalView) }
                .match(PingMessage::class.java) { sender.tell(true, sender) }
                .build()

    }
}