package partialview
import akka.actor.ActorContext
import akka.actor.ActorRef
import partialview.protocols.crashrecovery.CrashRecovery
import partialview.protocols.crashrecovery.NeighborRequestResult
import partialview.protocols.crashrecovery.Priority
import partialview.protocols.entropy.Entropy
import partialview.protocols.gossip.Gossip
import partialview.protocols.gossip.messages.GossipMessage
import partialview.protocols.gossip.messages.StatusMessageWrapper
import partialview.protocols.membership.Membership
import partialview.protocols.suffle.Shuffle
import partialview.wrappers.PVDependenciesWrapper
import java.util.*

class PartialView(pvWrapper: PVDependenciesWrapper, context: ActorContext, self: ActorRef) {

    private val viewOperations = ViewOperations(pvWrapper.activeView, pvWrapper.passiveView, pvWrapper.passiveActiveView,self, context, pvWrapper.mCounter)
    private val crashRecovery = CrashRecovery(pvWrapper.activeView, pvWrapper.passiveView, self, viewOperations, pvWrapper.globalViewActor, pvWrapper.mCounter)
    private val shuffle = Shuffle(pvWrapper.activeView, pvWrapper.passiveView, viewOperations, self, pvWrapper.mCounter)
    private val membership = Membership(pvWrapper.activeView, viewOperations, self, crashRecovery, pvWrapper.globalViewActor, pvWrapper.mCounter)
    private val entropy = Entropy(pvWrapper.activeView, crashRecovery)
    private val gossip = Gossip(pvWrapper.activeView, self, pvWrapper.globalViewActor)

    fun joinReceived(sender: ActorRef, newGlobalActor: ActorRef) {
        membership.join(sender, newGlobalActor)
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

    fun broadcast(message: StatusMessageWrapper) {
        gossip.broadcast(message)
    }

    fun gossipMessageReceived(message: GossipMessage) {
        gossip.gossipMessage(message)
    }

    fun cutTheWireReceived(disconnectNodeID: String) {
        entropy.cutTheWire(disconnectNodeID)
    }

    fun killReceived() {
        entropy.kill()
    }
}