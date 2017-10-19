package partialview.crashrecoveryprotocol

import akka.actor.ActorRef
import akkanetwork.AkkaUtils
import partialview.PVHelpers
import partialview.ViewsOperations
import partialview.crashrecoveryprotocol.messages.HelpMeMessage
import partialview.crashrecoveryprotocol.messages.HelpMeResponseMessage

class CrashRecovery(private var activeView: MutableSet<ActorRef>,
                    private var passiveView: MutableSet<ActorRef>,
                    private var self: ActorRef,
                    private var viewsOperations : ViewsOperations) {

    var deadNodesFromPassive = mutableSetOf<ActorRef>()

    fun crashed(node: ActorRef) {
        if(activeView.contains(node)) {
            viewsOperations.nodeFailedSoRemoveFromActive(node)
            val priority = if(activeView.size == 0) Priority.HIGH else Priority.LOW
            var actor = AkkaUtils.chooseRandom(passiveView)

            while(deadNodesFromPassive.contains(actor)) {
                deadNodesFromPassive.remove(actor)
                viewsOperations.nodeFailedSoRemoveFromPassive(actor)
                actor = AkkaUtils.chooseRandom(passiveView)
            }
            actor.tell(HelpMeMessage(priority), self)

        } else {
            deadNodesFromPassive.add(node)
        }
    }

    fun helpMeReceived(priority: Priority, sender: ActorRef) {
        var result = HelpResult.DECLINED

        // when 2 or more nodes fail and this node receives all those requests without notifying on time that node
        // (we can only accept 1 to join our active view)
        if(activeView.contains(sender)) {
            sender.tell(HelpMeResponseMessage(result), self)
        } else {
            if(priority == Priority.HIGH || !PVHelpers.activeViewisFull(activeView)) {
                viewsOperations.passiveToActive(sender)
                result = HelpResult.ACCEPTED
            }
            sender.tell(HelpMeResponseMessage(result), self)
        }
    }

    fun helpMeResponseReceived(result: HelpResult, sender: ActorRef) {
        if (result == HelpResult.ACCEPTED) {
            viewsOperations.passiveToActive(sender)
        } else {
            val actor = AkkaUtils.chooseRandomWithout(sender, passiveView)
            val priority = if(activeView.size == 0) Priority.HIGH else Priority.LOW
            actor.tell(HelpMeMessage(priority), self)
        }
    }
}