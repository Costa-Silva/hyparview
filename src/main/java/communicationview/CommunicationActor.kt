package communicationview

import akka.actor.AbstractActor
import akka.actor.Props
import communicationview.messages.GossipMessage
import communicationview.messages.StatusMessageWrapper
import communicationview.messages.UpdateActorMessage

class CommunicationActor(commWrapper: CommunicationWrapper): AbstractActor() {

    private val communication = Communication(self, commWrapper.globalActor, commWrapper.commMessages)

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
                .match(UpdateActorMessage::class.java) { communication.updateActor(it.node, it.event) }
                .build()
    }

}