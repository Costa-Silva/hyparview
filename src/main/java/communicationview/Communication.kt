package communicationview

import akka.actor.ActorContext
import akka.actor.ActorPath
import akka.actor.ActorRef
import akkanetwork.AkkaConstants.Companion.COMM_ACTOR
import akkanetwork.AkkaUtils
import akkanetwork.AkkaUtils.Companion.createNodeID
import communicationview.ActorUpdateEvent.DELETE_ACTOR
import communicationview.ActorUpdateEvent.NEW_ACTOR
import communicationview.messages.GossipMessage
import communicationview.messages.StatusMessageWrapper
import communicationview.wrappers.CommunicationMessages
import communicationview.wrappers.CommSharedData

class Communication(private val commWrapper: CommSharedData,
                    private val comMessages: CommunicationMessages,
                    private val context: ActorContext,
                    private val availableActors: MutableSet<String>) {

    fun updateActor(nodePathPartial: ActorPath, event: ActorUpdateEvent) {
        val nodeID= createNodeID(nodePathPartial.name())
        if (nodeID!= null) {
            val nodePath = AkkaUtils.createActorPathFrom(nodeID, COMM_ACTOR)
            when(event) {
                NEW_ACTOR -> availableActors.add(nodePath)
                DELETE_ACTOR -> availableActors.remove(nodePath)
            }
        }
    }

    fun broadcast(message: StatusMessageWrapper) {
        availableActors.forEach {
            comMessages.sent++
            context.actorSelection(it).tell(GossipMessage(message), ActorRef.noSender())
        }
    }

    fun gossipMessageReceived(message: GossipMessage) {
        comMessages.received++
        commWrapper.globalActor?.tell(message.message, ActorRef.noSender())
    }
}