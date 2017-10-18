package partialview.crashrecoveryprotocol

import akka.actor.ActorRef
import akkanetwork.AkkaUtils
import partialview.PVHelpers
import partialview.ViewsOperations
import partialview.crashrecoveryprotocol.messages.HelpMeMessage
import partialview.crashrecoveryprotocol.messages.HelpMeResponseMessage
import java.util.*

class CrashRecovery(private var activeView: MutableSet<ActorRef>,
                    private var passiveView: MutableSet<ActorRef>,
                    private var self: ActorRef,
                    private var viewsOperations : ViewsOperations) {

    private var ongoingHelpRequests = mutableSetOf<UUID>()

    fun helpMeReceived(requestUUID: UUID, priority: Priority, sender: ActorRef) {
        var result = HelpResult.DECLINED
        if(priority == Priority.HIGH || !PVHelpers.activeViewisFull(activeView)) {
            viewsOperations.addNodeActiveView(sender)
            result = HelpResult.ACCEPTED
        }
        sender.tell(HelpMeResponseMessage(requestUUID, result), self)
    }

    fun helpMeResponseReceived(requestUUID: UUID, result: HelpResult, sender: ActorRef) {
        if (result == HelpResult.ACCEPTED) {
            ongoingHelpRequests.remove(requestUUID)
            viewsOperations.addNodeActiveView(sender)
        } else {
            val actor = AkkaUtils.chooseRandomWithout(sender, passiveView)
            val priority = if(activeView.size == 0) Priority.HIGH else Priority.LOW
            actor.tell(HelpMeMessage(requestUUID, priority), self)
        }
    }

    fun getNewActiveNode(priority: Priority) {
        val helpRequestUUID = UUID.randomUUID()
        ongoingHelpRequests.add(helpRequestUUID)
        val actor = AkkaUtils.chooseRandom(passiveView)
        actor.tell(HelpMeMessage(helpRequestUUID, priority), self)
    }
}