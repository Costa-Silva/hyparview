package partialview.protocols.crashrecovery

import akka.actor.ActorRef
import akkanetwork.AkkaUtils
import partialview.PVHelpers
import partialview.ViewOperations
import partialview.protocols.crashrecovery.messages.HelpMeMessage
import partialview.protocols.crashrecovery.messages.HelpMeReplyMessage

class CrashRecovery(private var activeView: MutableSet<ActorRef>,
                    private var passiveView: MutableSet<ActorRef>,
                    private var self: ActorRef,
                    private var viewOperations: ViewOperations) {

    var deadNodesFromPassive = mutableSetOf<ActorRef>()

    fun crashed(node: ActorRef) {
        if(activeView.contains(node)) {
            viewOperations.nodeFailedSoRemoveFromActive(node)
            val priority = if(activeView.size == 0) Priority.HIGH else Priority.LOW
            sendHelpMe(priority)
        } else {
            deadNodesFromPassive.add(node)
        }
    }


    fun sendHelpMe(priority: Priority) {
        var actor = AkkaUtils.chooseRandom(passiveView)

        while(deadNodesFromPassive.contains(actor)) {
            deadNodesFromPassive.remove(actor)
            viewOperations.nodeFailedSoRemoveFromPassive(actor)
            actor = AkkaUtils.chooseRandom(passiveView)
        }
        actor?.tell(HelpMeMessage(priority), self)
    }

    // TODO: NeighborRequest
    fun helpMe(priority: Priority, sender: ActorRef) {
        var result = HelpResult.DECLINED

        // when 2 or more nodes fail and this node receives all those requests without notifying on time that node
        // (we can only accept 1 to join our active view)
        if(activeView.contains(sender)) {
            sender.tell(HelpMeReplyMessage(result), self)
        } else {
            if(priority == Priority.HIGH || !PVHelpers.activeViewisFull(activeView)) {
                viewOperations.passiveToActive(sender)
                result = HelpResult.ACCEPTED
            }
            sender.tell(HelpMeReplyMessage(result), self)
        }
    }

    fun helpMeResponse(result: HelpResult, sender: ActorRef) {
        if (result == HelpResult.ACCEPTED) {
            viewOperations.passiveToActive(sender)
        } else {
            val actor = AkkaUtils.chooseRandomWithout(sender, passiveView)
            val priority = if(activeView.size == 0) Priority.HIGH else Priority.LOW
            actor?.tell(HelpMeMessage(priority), self)
        }
    }
}