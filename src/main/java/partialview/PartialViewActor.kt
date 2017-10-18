package partialview

import akka.actor.AbstractActor
import akka.actor.ActorPath
import akka.actor.Props
import akkanetwork.AkkaConstants
import akkanetwork.AkkaUtils
import akkanetwork.AkkaUtils.Companion.chooseRandom
import akkanetwork.AkkaUtils.Companion.chooseRandomWithout
import akkanetwork.NodeID
import partialview.messages.DisconnectMessage
import partialview.messages.ForwardJoinMessage
import partialview.messages.JoinMessage

class PartialViewActor(contactNode: NodeID?, val fanout: Int,
                       val partialView: PartialView = PartialView()) : AbstractActor() {

    companion object {
        fun props(contactNode: NodeID?, fanout: Int): Props {
            return Props.create(PartialViewActor::class.java) { PartialViewActor(contactNode, fanout)}
        }
    }

    init {
        // Ignore when it's the contact node joining the system
        if(contactNode != null) {
            val contactRemote = AkkaUtils.lookUpRemote(context, AkkaConstants.SYSTEM_NAME, contactNode, contactNode.identifier)
            contactRemote.tell(JoinMessage(), self)
        }
    }

    override fun createReceive(): Receive {
        return receiveBuilder()
                .match(JoinMessage::class.java) { JoinReceived() }
                .match(ForwardJoinMessage::class.java) { forwardJoinReceived(it.timeToLive, it.newNode) }
                .match(DisconnectMessage::class.java) { deleteReceived()}
                .build()
    }

    fun JoinReceived() {
        addNodeActiveView(sender.path())
        // TODO: Global new node
        partialView.activeView.forEach {
            if (it != sender.path()) {
                val actor = context.actorSelection(it)
                actor.tell(ForwardJoinMessage(sender.path(), PVHelpers.ARWL), self)
            }
        }
    }

    fun forwardJoinReceived(timeToLive: Int, newNode: ActorPath) {
        if (timeToLive == 0 || partialView.activeView.size == 1) {
            addNodeActiveView(newNode)
        } else {
            if(timeToLive == PVHelpers.PRWL) {
                addNodePassiveView(newNode)
            }
            val randomNeighbor = chooseRandomWithout(sender.path(), partialView.activeView)
            val actor = context.actorSelection(randomNeighbor)
            actor.tell(ForwardJoinMessage(newNode, timeToLive - 1), self)
        }
    }

    fun deleteReceived() {
        val nodePath = sender.path()
        if (partialView.activeView.contains(nodePath)){
            partialView.activeView.remove(nodePath)
            addNodePassiveView(nodePath)
        }
    }

    fun addNodeActiveView(nodePath: ActorPath) {
        if(nodePath != self.path() && !partialView.activeView.contains(nodePath)) {
            if(PVHelpers.activeViewisFull(partialView.activeView)) {
                dropRandomElementFromActiveView()
            }
            partialView.activeView.add(nodePath)
        }
    }
    fun addNodePassiveView(nodePath: ActorPath) {
        if(nodePath != self.path() && !partialView.activeView.contains(nodePath) &&
                !partialView.passiveView.contains(nodePath)) {
            if(PVHelpers.passiveViewisFull(partialView.passiveView)) {
                partialView.passiveView.remove(chooseRandom(partialView.passiveView))
            }
            partialView.passiveView.add(nodePath)
        }
    }

    fun dropRandomElementFromActiveView() {
        val nodePath = chooseRandom(partialView.activeView)
        val actor = context.actorSelection(nodePath)
        actor.tell(DisconnectMessage(), self)
        partialView.activeView.remove(nodePath)
        partialView.passiveView.add(nodePath)
    }
}