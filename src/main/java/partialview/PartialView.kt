package partialview
import akka.actor.ActorContext
import akka.actor.ActorRef
import akkanetwork.AkkaUtils
import partialview.PVHelpers.Companion.TTSHUFFLE_MS
import partialview.messages.BroadcastMessage
import partialview.messages.DiscoverContactRefMessage
import partialview.messages.ForwardJoinMessage
import partialview.protocols.crashrecovery.CrashRecovery
import partialview.protocols.crashrecovery.HelpResult
import partialview.protocols.crashrecovery.Priority
import partialview.protocols.suffle.Shuffle
import java.util.*

data class PartialView(private var activeView: MutableSet<ActorRef> = mutableSetOf(),
                       private var passiveView: MutableSet<ActorRef> = mutableSetOf(),
                       private var context: ActorContext,
                       private var self: ActorRef) {

    private var membershipOperations = MembershipOperations(activeView, passiveView, self, context)
    private var crashRecovery = CrashRecovery(activeView, passiveView, self, membershipOperations)
    private var shuffle = Shuffle(activeView, passiveView, self)

    init {
        val task = object : TimerTask() {
            override fun run() {
                shuffle.shufflePassiveView()
            }
        }
        Timer().schedule(task ,0, TTSHUFFLE_MS)
    }
    
    fun JoinReceived(sender: ActorRef) {
        sender.tell(DiscoverContactRefMessage(), self)
        membershipOperations.addNodeActiveView(sender)
        // TODO: Global new node
        activeView.forEach {
            if (it != sender) {
                it.tell(ForwardJoinMessage(sender, PVHelpers.ARWL), self)
            }
        }
    }

    fun DiscoverContactRefMessageReceived(sender: ActorRef) {
        membershipOperations.addNodeActiveView(sender)
    }

    fun forwardJoinReceived(timeToLive: Int, newNode: ActorRef, sender: ActorRef) {
        if (timeToLive == 0 || activeView.size == 1) {
            membershipOperations.addNodeActiveView(newNode)
        } else {
            if(timeToLive == PVHelpers.PRWL) {
                membershipOperations.addNodePassiveView(newNode)
            }
            val randomNeighbor = AkkaUtils.chooseRandomWithout(sender, activeView)
            randomNeighbor?.tell(ForwardJoinMessage(newNode, timeToLive - 1), self)
        }
    }

    fun disconnectReceived(sender: ActorRef) {
        if (activeView.contains(sender)){
            membershipOperations.activeToPassive(sender)
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

    fun shuffleReceived(sample: MutableSet<ActorRef>, timeToLive: Int, uuid: UUID, origin: ActorRef, sender: ActorRef) {
        shuffle.shuffleReceived(sample, timeToLive, uuid, origin, sender)
    }

    fun shuffleReplyReceived(sample: MutableSet<ActorRef>, uuid: UUID) {
        shuffle.shuffleReplyReceived(sample, uuid)
    }
}