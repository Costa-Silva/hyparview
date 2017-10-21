package globalview

import akka.actor.AbstractActor
import akka.actor.Props
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
                .match(StatusMessageWrapper::class.java) { globalView.comDeliver(it) }
                .build()

    }
}