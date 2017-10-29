package communicationview

import akka.actor.AbstractActor
import akka.actor.Props
import communicationview.messages.GossipMessage
import communicationview.messages.StatusMessageWrapper
import communicationview.messages.UpdateActorMessage
import communicationview.wrappers.CommunicationWrapper

class CommunicationActor(commWrapper: CommunicationWrapper): AbstractActor() {

    private val communication = Communication(commWrapper, commWrapper.commMessages, context, commWrapper.availableActors)

    companion object {
        fun props(commWrapper: CommunicationWrapper): Props {
            return Props.create(CommunicationActor::class.java) { CommunicationActor(commWrapper) }
        }
    }

    override fun createReceive(): Receive {
        return receiveBuilder()
                .match(StatusMessageWrapper::class.java) { communication.broadcast(it) }

                .match(GossipMessage::class.java) { communication.gossipMessageReceived(it) }

                // from partial view
                .match(UpdateActorMessage::class.java) { communication.updateActor(it.nodePath, it.event) }
                .build()
    }

}