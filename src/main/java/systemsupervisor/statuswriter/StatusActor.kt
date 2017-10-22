package systemsupervisor.statuswriter

import akka.actor.AbstractActor
import akka.actor.ActorRef
import akka.actor.Props
import akkanetwork.AkkaConstants
import akkanetwork.AkkaConstants.Companion.STATUS_ACTOR
import akkanetwork.AkkaUtils
import partialview.wrappers.PartialViewSharedData
import systemsupervisor.graph.NodeStateMessage
import systemsupervisor.statuswriter.messages.RequestFromAppMessage
import systemsupervisor.statuswriter.messages.RequestStatusMessage
import java.util.*

class StatusActor(private val partialViewData: PartialViewSharedData): AbstractActor() {

    private val appReferences = LinkedList<ActorRef>()

    companion object {
        fun props(partialViewData: PartialViewSharedData): Props {
            return Props.create(StatusActor::class.java) { StatusActor(partialViewData) }
        }
    }

    override fun createReceive(): Receive {
        return receiveBuilder()
                .match(RequestFromAppMessage::class.java) { requestFromAppReceived(it.node) }
                .match(RequestStatusMessage::class.java) { sender.tell(NodeStateMessage(partialViewData), self) }
                .match(NodeStateMessage::class.java) { nodeStateReceived(it) }
                .build()
    }

    private fun nodeStateReceived(nodeStateMessage: NodeStateMessage) {
        appReferences.removeFirst().tell(nodeStateMessage, self)
    }

    private fun requestFromAppReceived(node: String){
        val nodeID = AkkaUtils.createNodeID(node)
        nodeID?.let {
            val contactNode = AkkaUtils.lookUpRemote(context, AkkaConstants.SYSTEM_NAME,nodeID, STATUS_ACTOR)
            appReferences.add(sender)
            contactNode.tell(RequestStatusMessage(), self)
        }
    }
}