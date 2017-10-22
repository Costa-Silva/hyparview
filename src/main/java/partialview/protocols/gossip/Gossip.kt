package partialview.protocols.gossip

import akka.actor.ActorRef
import partialview.protocols.gossip.messages.GossipMessage
import partialview.protocols.gossip.messages.StatusMessageWrapper

class Gossip(private val activeView: MutableSet<ActorRef>,
             private val self: ActorRef,
             private val gvActor: ActorRef ) {

    var receivedMessages = 0
    var sentMessages = 0

    fun broadcast(message: StatusMessageWrapper) {
        sentMessages++
        activeView.forEach {
            it.tell(GossipMessage(message), self)
        }
    }

    fun gossipMessage(message: GossipMessage) {
        receivedMessages++
        gvActor.tell(message.message, ActorRef.noSender())
    }
}