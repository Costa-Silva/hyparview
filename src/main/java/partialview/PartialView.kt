package partialview
import akka.actor.ActorContext
import akka.actor.ActorRef
import partialview.PVHelpers.Companion.TTSHUFFLE_MS
import partialview.protocols.crashrecovery.CrashRecovery
import partialview.protocols.crashrecovery.NeighborRequestResult
import partialview.protocols.crashrecovery.Priority
import partialview.protocols.entropy.Entropy
import partialview.protocols.membership.Membership
import partialview.protocols.membership.messages.BroadcastMessage
import partialview.protocols.suffle.Shuffle
import java.util.*

class PartialView(private val pvWrapper: PVDependenciesWrapper, context: ActorContext, private var self: ActorRef) {

    private var viewOperations = ViewOperations(pvWrapper.activeView, pvWrapper.passiveView, self, context)
    private var crashRecovery = CrashRecovery(pvWrapper.activeView, pvWrapper.passiveView, self, viewOperations)
    private var shuffle = Shuffle(pvWrapper.activeView, pvWrapper.passiveView, self)
    private var membership = Membership(pvWrapper.activeView, viewOperations, self, crashRecovery)
    private var entropy = Entropy(pvWrapper.activeView, crashRecovery)

    init {
        val task = object : TimerTask() {
            override fun run() {
                shuffle.shufflePassiveView()
            }
        }
        Timer().schedule(task ,0, TTSHUFFLE_MS)
    }

    fun joinReceived(sender: ActorRef) {
        membership.join(sender)
    }

    fun discoverContactRefMessageReceived(sender: ActorRef) {
        membership.discoverContactRefMessage(sender)
    }

    fun forwardJoinReceived(timeToLive: Int, newNode: ActorRef, sender: ActorRef) {
        membership.forwardJoin(timeToLive, newNode, sender)
    }

    fun disconnectReceived(sender: ActorRef) {
        membership.disconnect(sender)
    }

    fun crashed(node: ActorRef) {
        crashRecovery.crashed(node)
    }

    fun helpMeReceived(priority: Priority, sender: ActorRef) {
        crashRecovery.neighborRequest(priority, sender)
    }

    fun helpMeResponseReceived(result: NeighborRequestResult, sender: ActorRef) {
        crashRecovery.neighborRequestReply(result, sender)
    }

    fun shuffleReceived(sample: MutableSet<ActorRef>, timeToLive: Int, uuid: UUID, origin: ActorRef, sender: ActorRef) {
        shuffle.shuffle(sample, timeToLive, uuid, origin, sender)
    }

    fun shuffleReplyReceived(sample: MutableSet<ActorRef>, uuid: UUID) {
        shuffle.shuffleReply(sample, uuid)
    }

    fun cutTheWireReceived(disconnectNodeID: String) {
        entropy.cutTheWire(disconnectNodeID)
    }

    fun killReceived() {
        entropy.kill()
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