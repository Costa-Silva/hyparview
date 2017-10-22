package globalview

import akka.actor.AbstractActor
import akka.actor.Props
import globalview.messages.ConflictMessage
import globalview.messages.GiveGlobalMessage
import partialview.protocols.gossip.messages.StatusMessageWrapper

class GlobalViewActor(gvWrapper: GVDependenciesActor): AbstractActor() {

    private val globalView = GlobalView(gvWrapper.eventList, gvWrapper.pendingEvents, gvWrapper.toRemove,
            gvWrapper.globalView, gvWrapper.pvActor, self)

    companion object {
        fun props(gvWrapper: GVDependenciesActor): Props {
            return Props.create(GlobalViewActor::class.java) { GlobalViewActor(gvWrapper) }
        }
    }

    override fun createReceive(): Receive {
        return receiveBuilder()
                .match(StatusMessageWrapper::class.java) { globalView.partialDeliver(it) }
                .match(GiveGlobalMessage::class.java) { globalView.giveGlobalReceived() }
                .match(ConflictMessage::class.java) { globalView.conflictMessageReceived(it.myGlobalView) }
                .build()

    }
}