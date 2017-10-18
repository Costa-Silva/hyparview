package partialview
import akka.actor.ActorRef
import akkanetwork.AkkaUtils
import partialview.crashrecoveryprotocol.HelpResult
import partialview.crashrecoveryprotocol.Priority
import partialview.crashrecoveryprotocol.messages.HelpMeMessage
import partialview.crashrecoveryprotocol.messages.HelpMeResponseMessage
import partialview.messages.BroadcastMessage
import partialview.messages.DisconnectMessage
import partialview.messages.DiscoverContactRefMessage
import partialview.messages.ForwardJoinMessage
import java.util.*

data class PartialView(private var activeView: MutableSet<ActorRef> = mutableSetOf(),
                       private var passiveView: MutableSet<ActorRef> = mutableSetOf(),
                       private var self: ActorRef){

    private var ongoingHelpRequests = mutableSetOf<UUID>()

    fun JoinReceived(sender: ActorRef) {
        sender.tell(DiscoverContactRefMessage(), self)
        addNodeActiveView(sender)
        // TODO: Global new node
        activeView.forEach {
            if (it != sender) {
                it.tell(ForwardJoinMessage(sender, PVHelpers.ARWL), self)
            }
        }
    }

    fun DiscoverContactRefMessageReceived(sender: ActorRef) {
        if (!activeView.contains(sender))
            activeView.add(sender)
    }

    fun forwardJoinReceived(timeToLive: Int, newNode: ActorRef, sender: ActorRef) {
        if (timeToLive == 0 || activeView.size == 1) {
            addNodeActiveView(newNode)
        } else {
            if(timeToLive == PVHelpers.PRWL) {
                addNodePassiveView(newNode)
            }
            val randomNeighbor = AkkaUtils.chooseRandomWithout(sender, activeView)
            randomNeighbor.tell(ForwardJoinMessage(newNode, timeToLive - 1), self)
        }
    }

    fun disconnectReceived(sender: ActorRef) {
        if (activeView.contains(sender)){
            activeView.remove(sender)
            addNodePassiveView(sender)
        }
    }

    fun broadcast(message: BroadcastMessage) {
        activeView.forEach {
            it.tell(message, self)
        }
    }

    fun broadcastReceived(message: BroadcastMessage, sender: ActorRef) {
        // TODO: partialDeliver (communication)
    }

    fun helpMeReceived(requestUUID: UUID, priority: Priority, sender: ActorRef) {
        var result = HelpResult.DECLINED
        if(priority == Priority.HIGH || !PVHelpers.activeViewisFull(activeView)) {
            addNodeActiveView(sender)
            result = HelpResult.ACCEPTED
        }
        sender.tell(HelpMeResponseMessage(requestUUID, result), self)
    }

    fun helpMeResponseReceived(requestUUID: UUID, result: HelpResult, sender: ActorRef) {
        if (result == HelpResult.ACCEPTED) {
            ongoingHelpRequests.remove(requestUUID)
            addNodeActiveView(sender)
        } else {
            val actor = AkkaUtils.chooseRandomWithout(sender, passiveView)
            val priority = if(activeView.size == 0) Priority.HIGH else Priority.LOW
            actor.tell(HelpMeMessage(requestUUID, priority), self)
        }
    }

    fun crashed(node: ActorRef) {
        activeView.remove(node)
        val priority = if(activeView.size == 0) Priority.HIGH else Priority.LOW
        getNewActiveNode(priority)
    }

    private fun getNewActiveNode(priority: Priority) {
        val helpRequestUUID = UUID.randomUUID()
        ongoingHelpRequests.add(helpRequestUUID)
        val actor = AkkaUtils.chooseRandom(passiveView)
        actor.tell(HelpMeMessage(helpRequestUUID, priority), self)
    }

    fun addNodeActiveView(node: ActorRef) {
        if(node != self && !activeView.contains(node)) {
            if(PVHelpers.activeViewisFull(activeView)) {
                dropRandomElementFromActiveView()
            }
            activeView.add(node)
        }
    }
    fun addNodePassiveView(node: ActorRef) {
        if(node != self && !activeView.contains(node) &&
                !passiveView.contains(node)) {
            if(PVHelpers.passiveViewisFull(passiveView)) {
                passiveView.remove(AkkaUtils.chooseRandom(passiveView))
            }
            passiveView.add(node)
        }
    }

    private fun dropRandomElementFromActiveView() {
        val node = AkkaUtils.chooseRandom(activeView)
        node.tell(DisconnectMessage(), self)
        activeView.remove(node)
        passiveView.add(node)
    }
}