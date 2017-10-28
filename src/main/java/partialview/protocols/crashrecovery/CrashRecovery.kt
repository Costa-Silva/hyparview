package partialview.protocols.crashrecovery

import akka.actor.ActorRef
import akka.actor.ActorSelection
import akka.pattern.Patterns
import akkanetwork.AkkaUtils
import globalview.GVHelpers
import globalview.messages.external.PingMessage
import globalview.messages.internal.MayBeDeadMessage
import partialview.PVHelpers
import partialview.PVMessagesCounter
import partialview.ViewOperations
import partialview.protocols.crashrecovery.messages.NeighborRequestMessage
import partialview.protocols.crashrecovery.messages.NeighborRequestReplyMessage
import scala.concurrent.Await
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit

class CrashRecovery(private val activeView: MutableSet<ActorRef>,
                    private val passiveView: MutableSet<ActorRef>,
                    private val passiveActiveView: MutableSet<ActorRef>,
                    private val self: ActorRef,
                    private val viewOperations: ViewOperations,
                    private val gvActor: ActorSelection,
                    private val mCounter: PVMessagesCounter) {

    private val ongoingNeighborRequests = mutableSetOf<ActorRef>()

    fun crashed(node: ActorRef) {
        if(activeView.contains(node)) {
            viewOperations.nodeFailedSoRemoveFromActive(node)
            val priority = if(activeView.size == 0) Priority.HIGH else Priority.LOW
            System.err.println("o $node morreu")
            sendNeighborRequest(priority)
            gvActor.tell(MayBeDeadMessage(node),self)
        } else if(passiveActiveView.contains(node)) {
            viewOperations.removeFromPassiveActive(node)
        }
    }

    fun sendNeighborRequest(priority: Priority) {
        val actor = AkkaUtils.chooseRandomWithout(ongoingNeighborRequests, passiveActiveView)
        actor?.let {
            mCounter.neighborRequestsSent++
            ongoingNeighborRequests.add(it)
            it.tell(NeighborRequestMessage(priority), self)
        }
    }

    fun neighborRequest(priority: Priority, sender: ActorRef) {
        mCounter.neighborRequestsReceived++
        var result = NeighborRequestResult.DECLINED
            if(priority == Priority.HIGH || !PVHelpers.activeViewisFull(activeView)) {
                viewOperations.passiveToActive(sender)
                result = NeighborRequestResult.ACCEPTED
            }
            sender.tell(NeighborRequestReplyMessage(result), self)
    }

    fun neighborRequestReply(result: NeighborRequestResult, sender: ActorRef) {
        if (result == NeighborRequestResult.ACCEPTED) {
            viewOperations.passiveToActive(sender)
        } else {
            var actorFromPassive = false
            var actor = AkkaUtils.chooseRandomWithout(ongoingNeighborRequests, passiveActiveView)

            if(actor == null) {
                actor = AkkaUtils.chooseRandomWithout(ongoingNeighborRequests, passiveView)
                actorFromPassive = true
            }

            actor?.let {
                if (actorFromPassive) {
                    try {
                        val future = Patterns.ask(it, PingMessage(), GVHelpers.CHECK_IF_ALIVE_TIMEOUT_MS)
                        Await.result(future, FiniteDuration(GVHelpers.CHECK_IF_ALIVE_TIMEOUT_MS, TimeUnit.MILLISECONDS)) as Boolean
                    } catch (e: Exception) {
                        System.err.println("Dead from passive")
                        viewOperations.removeFromPassiveActive(it)
                        return
                    }
                }
                val priority = if(activeView.size == 0) Priority.HIGH else Priority.LOW
                mCounter.neighborRequestsSent++
                ongoingNeighborRequests.add(it)
                it.tell(NeighborRequestMessage(priority), self)
            }
        }
        ongoingNeighborRequests.remove(sender)
    }
}