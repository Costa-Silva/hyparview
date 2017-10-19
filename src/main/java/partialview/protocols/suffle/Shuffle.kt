package partialview.protocols.suffle

import akka.actor.ActorRef
import akkanetwork.AkkaUtils
import partialview.PVHelpers
import partialview.PVHelpers.Companion.PASSIVE_VIEW_MAX_SIZE
import partialview.protocols.suffle.messages.ShuffleMessage
import partialview.protocols.suffle.messages.ShuffleReplyMessage
import java.util.*

class Shuffle(private var activeView: MutableSet<ActorRef>,
              private var passiveView: MutableSet<ActorRef>,
              private var self: ActorRef) {

    private val samplesSent = mutableMapOf<UUID, MutableSet<ActorRef>>()

    fun shufflePassiveView() {
        val sample = mutableSetOf<ActorRef>()
        val uuid = UUID.randomUUID()
        val activeNodesToFind = Math.min(PVHelpers.N_ACTIVE_NODES_SHUFF, activeView.size)
        val passiveNodesToFind = Math.min(PVHelpers.N_PASSIVE_NODES_SHUFF, passiveView.size)
        val activeNodes = populateSample(activeNodesToFind, activeView)
        val passiveNodes = populateSample(passiveNodesToFind, passiveView)

        sample.addAll(activeNodes)
        sample.addAll(passiveNodes)
        sample.add(self)

        val actor = AkkaUtils.chooseRandom(activeView)
        actor?.let {
            samplesSent.put(uuid, sample)
            it.tell(ShuffleMessage(sample, PVHelpers.SHUFFLE_TTL, uuid, self), self)
        }
    }

    fun shuffleReceived(sample: MutableSet<ActorRef>, timeToLive: Int, uuid: UUID, origin: ActorRef, sender: ActorRef) {
        val newTLL = timeToLive - 1
        if (newTLL > 0 && activeView.size > 1) {
            val actor = AkkaUtils.chooseRandomWithout(mutableSetOf(origin, sender), activeView)
            actor?.tell(ShuffleMessage(sample, newTLL, uuid, origin), self)
        } else {
            val passiveNodesToFind = Math.min(sample.size - 1, passiveView.size)
            val randomPassiveNodes = populateSample(passiveNodesToFind, passiveView)
            randomPassiveNodes.add(self)

            val myRandomPassiveNodes = mutableSetOf<ActorRef>()
            myRandomPassiveNodes.addAll(randomPassiveNodes)

            removeAlreadyKnownNodes(sample)
            replaceNodesInPassiveView(sample, randomPassiveNodes)
            origin.tell(ShuffleReplyMessage(myRandomPassiveNodes, uuid), self)
        }
    }

    private fun replaceNodesInPassiveView(sample: MutableSet<ActorRef>, randomPassiveNodes: MutableSet<ActorRef>) {
        val sumNodes = passiveView.size + sample.size
        val nodesToRemove = if(sumNodes > PASSIVE_VIEW_MAX_SIZE) (sumNodes-PASSIVE_VIEW_MAX_SIZE) else 0
        val removedNodes = mutableSetOf<ActorRef>()
        for ( i in 0 until nodesToRemove) {
            var actor: ActorRef? = null
            while (actor == null || removedNodes.contains(actor)) {
                actor = AkkaUtils.chooseRandomWithout(removedNodes, randomPassiveNodes)
                if(actor == null) {actor = AkkaUtils.chooseRandom(passiveView)} else {randomPassiveNodes.remove(actor)}
            }
            removedNodes.add(actor)
            passiveView.remove(actor)
        }
        passiveView.addAll(sample)
    }

    fun shuffleReplyReceived(sample: MutableSet<ActorRef>, uuid: UUID) {
        samplesSent.remove(uuid)?.let { setSent ->
            removeAlreadyKnownNodes(sample)
            replaceNodesInPassiveView(sample, setSent)
        }
    }

    private fun removeAlreadyKnownNodes(sample: MutableSet<ActorRef>) {
        sample.removeAll { it == self || activeView.contains(it) || passiveView.contains(it) }
    }

    private fun populateSample(nodesToFind: Int, viewSet: Set<ActorRef>): MutableSet<ActorRef> {
        val resultSet = mutableSetOf<ActorRef>()
        if(nodesToFind > 0) {
            while (resultSet.size < nodesToFind) {
                val actor = AkkaUtils.chooseRandom(viewSet)
                if(actor != null)
                    resultSet.add(actor)
            }
        }
        return resultSet
    }
}