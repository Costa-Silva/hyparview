package partialview

import akka.actor.ActorContext
import akka.actor.ActorRef
import akkanetwork.AkkaUtils
import partialview.protocols.membership.messages.DisconnectMessage
import java.util.*

class ViewOperations(private var activeView: MutableSet<ActorRef>,
                     private var passiveView: MutableSet<ActorRef>,
                     private var passiveActiveView: MutableSet<ActorRef>,
                     private var self: ActorRef,
                     private var context: ActorContext) {

    private var timer = Timer()
    private var watchSet = mutableSetOf<ActorRef>()

    init {
        startShufflePassiveActive()
    }

    fun addNodeActiveView(node: ActorRef) {
        if(node != self && !activeView.contains(node)) {
            if(PVHelpers.activeViewisFull(activeView)) {
                dropRandomElementFromActiveView()
            }
            addToWatchSet(node)
            activeView.add(node)
        }
    }

    fun addNodePassiveView(node: ActorRef) {
        if(node != self && !activeView.contains(node) &&
                !passiveView.contains(node)) {
            if(PVHelpers.passiveViewisFull(passiveView)) {
                // Search from passive view first to drop element else drop from our passive active
                var actor = AkkaUtils.chooseRandomWithout(passiveActiveView, passiveView)
                if (actor == null) {
                    actor = AkkaUtils.chooseRandom(passiveActiveView)
                }
                actor?.let {
                    if (passiveActiveView.contains(it)) {
                        removeFromPassiveActive(it)
                    } else {
                        passiveView.remove(it)
                    }
                }
            }
            passiveView.add(node)
        }
    }

    fun passiveToActive(node: ActorRef) {
        if (passiveActiveView.contains(node)) {
            removeFromPassiveActive(node)
        } else {
            passiveView.remove(node)
        }
        addNodeActiveView(node)
    }

    fun activeToPassive(node: ActorRef) {
        activeView.remove(node)
        addNodePassiveView(node)
    }

    fun nodeFailedSoRemoveFromActive(node: ActorRef) {
        activeView.remove(node)
        removeFromWatchSet(node)
    }

    fun removeFromPassiveActive(node: ActorRef) {
        stopShufflePassiveActive()
        passiveView.remove(node)
        passiveActiveView.remove(node)
        removeFromWatchSet(node)
        startShufflePassiveActive()
    }

    fun replaceNodesInPassiveViewShuffle(nodesToRemove: MutableSet<ActorRef>, nodesToAdd: MutableSet<ActorRef>) {
        stopShufflePassiveActive()
        passiveView.addAll(nodesToAdd)
        passiveView.removeAll(nodesToRemove)
        startShufflePassiveActive()
    }

    private fun dropRandomElementFromActiveView() {
        val node = AkkaUtils.chooseRandom(activeView)
        node?.let {
            it.tell(DisconnectMessage(), self)
            activeToPassive(it)
        }
    }

    private fun addToWatchSet(node: ActorRef) {
        if(!watchSet.contains(node)) {
            watchSet.add(node)
            context.watch(node)
        }
    }

    private fun removeFromWatchSet(node: ActorRef) {
        if(watchSet.contains(node)) {
            watchSet.remove(node)
            context.unwatch(node)
        }
    }

    private fun startShufflePassiveActive() {
        timer = Timer()

        val shufflePassiveActiveTask = object : TimerTask() {
            override fun run() {
                shufflePassiveActive()
            }
        }

        timer.schedule(shufflePassiveActiveTask, 0, PVHelpers.TTSHUFFLE_MS)
    }

    private fun stopShufflePassiveActive() {
        timer.cancel()
    }

    private fun shufflePassiveActive() {
        val passiveActiveNodes = Math.min(passiveView.size, PVHelpers.ACTIVE_PASSIVE_VIEW_SIZE)
        val newValues = mutableSetOf<ActorRef>()
        for (i in 0 until passiveActiveNodes) {
            AkkaUtils.chooseRandomWithout(newValues, passiveView)?.let { newValues.add(it) }
        }

        newValues.toMutableSet().forEach {
            if(!passiveActiveView.contains(it)) {
                passiveActiveView.add(it)
                addToWatchSet(it)
            }
        }

        passiveActiveView.toMutableSet().forEach {
            if(!newValues.contains(it)) {
                passiveActiveView.remove(it)
                removeFromWatchSet(it)
            }
        }
    }
}