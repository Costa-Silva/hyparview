package partialview
import akka.actor.ActorContext
import akka.actor.ActorRef
import akkanetwork.AkkaUtils
import partialview.crashrecoveryprotocol.CrashRecovery
import partialview.crashrecoveryprotocol.HelpResult
import partialview.crashrecoveryprotocol.Priority
import partialview.messages.BroadcastMessage
import partialview.messages.DiscoverContactRefMessage
import partialview.messages.ForwardJoinMessage

data class PartialView(private var activeView: MutableSet<ActorRef> = mutableSetOf(),
                       private var passiveView: MutableSet<ActorRef> = mutableSetOf(),
                       private var context: ActorContext,
                       private var self: ActorRef){

    private var viewsOperations = ViewsOperations(activeView, passiveView, self, context)
    private var crashRecovery = CrashRecovery(activeView, passiveView, self, viewsOperations)

    fun JoinReceived(sender: ActorRef) {
        sender.tell(DiscoverContactRefMessage(), self)
        viewsOperations.addNodeActiveView(sender)
        // TODO: Global new node
        activeView.forEach {
            if (it != sender) {
                it.tell(ForwardJoinMessage(sender, PVHelpers.ARWL), self)
            }
        }
    }

    fun DiscoverContactRefMessageReceived(sender: ActorRef) {
        viewsOperations.addNodeActiveView(sender)
    }

    fun forwardJoinReceived(timeToLive: Int, newNode: ActorRef, sender: ActorRef) {
        if (timeToLive == 0 || activeView.size == 1) {
            viewsOperations.addNodeActiveView(newNode)
        } else {
            if(timeToLive == PVHelpers.PRWL) {
                viewsOperations.addNodePassiveView(newNode)
            }
            val randomNeighbor = AkkaUtils.chooseRandomWithout(sender, activeView)
            randomNeighbor.tell(ForwardJoinMessage(newNode, timeToLive - 1), self)
        }
    }

    fun disconnectReceived(sender: ActorRef) {
        if (activeView.contains(sender)){
            viewsOperations.activeToPassive(sender)
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

    fun crashed(node: ActorRef) {
        crashRecovery.crashed(node)
    }

    fun helpMeReceived(priority: Priority, sender: ActorRef) {
        crashRecovery.helpMeReceived(priority, sender)
    }

    fun helpMeResponseReceived(result: HelpResult, sender: ActorRef) {
        crashRecovery.helpMeResponseReceived(result, sender)
    }
}