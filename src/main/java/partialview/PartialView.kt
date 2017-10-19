package partialview
import akka.actor.ActorContext
import akka.actor.ActorRef
import partialview.PVHelpers.Companion.TTSHUFFLE_MS
import partialview.protocols.crashrecovery.CrashRecovery
import partialview.protocols.crashrecovery.HelpResult
import partialview.protocols.crashrecovery.Priority
import partialview.protocols.membership.Membership
import partialview.protocols.membership.messages.BroadcastMessage
import partialview.protocols.suffle.Shuffle
import java.util.*

class PartialView(private val pvWrapper: PVDependenciesWrapper, context: ActorContext, private var self: ActorRef) {

    private var viewOperations = ViewOperations(pvWrapper.activeView, pvWrapper.passiveView, self, context)
    private var crashRecovery = CrashRecovery(pvWrapper.activeView, pvWrapper.passiveView, self, viewOperations)
    private var shuffle = Shuffle(pvWrapper.activeView, pvWrapper.passiveView, self)
    private var membership = Membership(pvWrapper.activeView, viewOperations, self)

    init {
        val task = object : TimerTask() {
            override fun run() {
                shuffle.shufflePassiveView()
            }
        }
        Timer().schedule(task ,0, TTSHUFFLE_MS)
    }

    fun joinReceived(sender: ActorRef) {
        membership.joinReceived(sender)
    }

    fun discoverContactRefMessageReceived(sender: ActorRef) {
        membership.discoverContactRefMessageReceived(sender)
    }

    fun forwardJoinReceived(timeToLive: Int, newNode: ActorRef, sender: ActorRef) {
        membership.forwardJoinReceived(timeToLive, newNode, sender)
    }

    fun disconnectReceived(sender: ActorRef) {
        membership.disconnectReceived(sender)
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

    fun broadcast(message: BroadcastMessage) {
        pvWrapper.activeView.forEach {
            it.tell(message, self)
        }
    }

    fun broadcastReceived(message: BroadcastMessage, sender: ActorRef) {
        // TODO: partialDeliver (communication)
    }
}