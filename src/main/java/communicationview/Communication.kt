package communicationview

import akka.actor.ActorRef
import communicationview.ActorUpdateEvent.DELETE_ACTOR
import communicationview.ActorUpdateEvent.NEW_ACTOR
import communicationview.messages.GossipMessage
import communicationview.messages.StatusMessageWrapper

class Communication(val self: ActorRef,
                    private val gvActor: ActorRef?,
                    private val comMessages: CommunicationMessages) {

    private val availableActors = mutableSetOf<ActorRef>()

    fun updateActor(node: ActorRef, event: ActorUpdateEvent) {
        when(event) {
            NEW_ACTOR -> availableActors.add(node)
            DELETE_ACTOR -> availableActors.remove(node)
        }
    }

    fun broadcast(message: StatusMessageWrapper) {
        availableActors.forEach {
            comMessages.sent++
            it.tell(GossipMessage(message), self)
        }
    }

    fun gossipMessageReceived(message: GossipMessage) {
        comMessages.received++
        gvActor?.tell(message.message, ActorRef.noSender())
    }
}