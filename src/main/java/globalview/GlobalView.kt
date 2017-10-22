package globalview

import akka.actor.ActorRef
import akka.actor.ActorSelection
import akka.pattern.Patterns
import akkanetwork.AkkaUtils
import globalview.GVHelpers.Companion.CHECK_IF_ALIVE_TIMEOUT_MS
import globalview.GVHelpers.Companion.MAY_BE_DEAD_PERIOD_MS
import globalview.GVHelpers.Companion.SEND_EVENTS_PERIOD_MS
import globalview.GVHelpers.Companion.SEND_HASH_PERIOD_MS
import globalview.GVHelpers.Companion.eventListisFull
import globalview.GVHelpers.Companion.pendingEventsisFull
import globalview.messages.*
import partialview.protocols.gossip.messages.StatusMessageWrapper
import scala.concurrent.Await
import scala.concurrent.duration.FiniteDuration
import java.util.*
import java.util.concurrent.TimeUnit

class GlobalView(private val eventList: LinkedList<UUID>,
                 private val pendingEvents: MutableMap<UUID, Event>,
                 private val toRemove: MutableSet<ActorRef>,
                 private val globalView: MutableSet<ActorRef>,
                 private val self: ActorRef,
                 private val pvActor: ActorSelection) {

    val timerSendEvents = Timer()
    val timersMayBeDead = mutableMapOf<UUID, Timer>()

    private val sendEventsTask = object : TimerTask() {
        override fun run() {
            sendEvents()
        }
    }

    init {
        // TODO: Test if this is needed
        Thread.sleep(1000)
        val sendHash = object : TimerTask() {
            override fun run() {
                timerSendEvents.cancel()
                globalBroadcast()
            }
        }
        Timer().scheduleAtFixedRate(sendHash ,0, SEND_HASH_PERIOD_MS)
    }

    fun sendEvents() {
        globalBroadcast()
    }

    fun globalBroadcast() {
        val message = StatusMessage(globalView.hashCode(), pendingEvents, toRemove.isEmpty())
        pvActor.tell(StatusMessageWrapper(message, self), ActorRef.noSender())
    }

    private fun globalAdd(newNode: ActorRef, needsGlobal: Boolean) {
        globalView.add(newNode)
        if (needsGlobal) {
            sendGlobalMessage(newNode)
        }
    }

    fun receivedGlobalMessage(newView: MutableSet<ActorRef>, eventIds: LinkedList<UUID>) {
        globalView.clear()
        eventList.clear()
        globalView.addAll(newView)
        eventList.addAll(eventIds)
    }

    private fun sendGlobalMessage(node: ActorRef) {
        node.tell(GlobalMessage(globalView, eventList), self)
    }

    fun remove(node: ActorRef) {
        globalView.remove(node)
        toRemove.remove(node)
    }

    fun globalMayBeDead(node: ActorRef) {
        val uuid = UUID.randomUUID()
        addToEventList(uuid, Event(EventEnum.MAY_BE_DEAD, node))
        toRemove.add(node)

        val timer = Timer()
        val removeNode = object : TimerTask() {
            override fun run() {
                remove(node)
                timersMayBeDead.remove(uuid)
            }
        }
        timer.schedule(removeNode, MAY_BE_DEAD_PERIOD_MS)
        timersMayBeDead.put(uuid, timer)
    }

    private fun globalNewNode(newNode: ActorRef, needsGlobal: Boolean) {
        val uuid = UUID.randomUUID()
        addToEventList(uuid, Event(EventEnum.NEW_NODE, newNode))
        globalAdd(newNode, needsGlobal)
    }

    private fun addToEventList(eventId: UUID, event: Event) {
        if(pendingEvents.isEmpty()) {
            timerSendEvents.schedule(sendEventsTask, SEND_EVENTS_PERIOD_MS)
        } else if(event.node == self && event.event == EventEnum.MAY_BE_DEAD) {
            timerSendEvents.cancel()
            timerSendEvents.schedule(sendEventsTask, SEND_EVENTS_PERIOD_MS)
        }

        if (eventListisFull(eventList)) {
            eventList.removeFirst()
        }
        eventList.add(eventId)
        pendingEvents.put(eventId, event)

        if(pendingEventsisFull(pendingEvents)) {
            timerSendEvents.cancel()
            globalBroadcast()
        }
    }

    private fun imAlive() {
        addToEventList(UUID.randomUUID(), Event(EventEnum.STILL_ALIVE, self))
    }

    fun partialDeliver(message: StatusMessageWrapper) {
        val hash = message.statusMessage.hash
        val newEvents = message.statusMessage.pendingEvents
        val toRemoveIsEmpty = message.statusMessage.toRemoveIsEmpty
        val senderID = message.sender

        var compareHash = false

        if (pendingEvents.isEmpty() && toRemoveIsEmpty) {
            compareHash = true
        }

        val gottaGoFastSet = mutableSetOf<UUID>()
        gottaGoFastSet.addAll(eventList)


        newEvents.forEach {
            val removeNodeTask = object : TimerTask() {
                override fun run() {
                    remove(it.value.node)
                    timersMayBeDead.remove(it.key)
                }
            }

            if(!gottaGoFastSet.contains(it.key)) {
                addToEventList(it.key, it.value)
                val type = it.value.event
                val node = it.value.node
                if (type == EventEnum.NEW_NODE) {
                    globalAdd(node, false)
                } else if (type == EventEnum.MAY_BE_DEAD) {
                    if(node != self) {
                        toRemove.add(node)
                        val timer = Timer()
                        timer.schedule(removeNodeTask, MAY_BE_DEAD_PERIOD_MS)
                        timersMayBeDead.put(it.key, timer)
                    } else {
                      imAlive()
                    }
                } else if (type == EventEnum.STILL_ALIVE) {
                    if(toRemove.contains(node)) {
                        toRemove.remove(node)
                        timersMayBeDead.remove(it.key)?.cancel()
                    } else {
                        globalAdd(it.value.node, false)
                    }
                }

            }
        }
        if(compareHash && toRemove.isEmpty()) {
            globalCompare(hash,senderID)
        }
    }

    private fun globalCompare(hash: Int, sender: ActorRef) {
        val myHash = globalView.hashCode()
        if (myHash != hash) {
            val myNumber = AkkaUtils.numberFromIdentifier(self.path().name())
            val enemyNumber = AkkaUtils.numberFromIdentifier(sender.path().name())
            if (enemyNumber>myNumber) {
                sender.tell(ConflictMessage(globalView), self)
            } else {
                sender.tell(GiveGlobalMessage(), self)
            }
        }
    }

    fun giveGlobalReceived(sender: ActorRef) {
        sender.tell(ConflictMessage(globalView), self)
    }

    // TODO: TEST THIS ASK
    fun conflictMessageReceived(otherGlobalView: MutableSet<ActorRef>) {
        otherGlobalView
                .filter { !globalView.contains(it) }
                .forEach { node ->
                    val future = Patterns.ask(node, PingMessage(), CHECK_IF_ALIVE_TIMEOUT_MS)
                    val result = Await.result(future, FiniteDuration(CHECK_IF_ALIVE_TIMEOUT_MS, TimeUnit.MILLISECONDS))
                    val isAlive = result != null
                    if(isAlive) {
                        globalNewNode(node, false)
                    } else {
                        addToEventList(UUID.randomUUID(), Event(EventEnum.MAY_BE_DEAD, node))
                    }
                }
        globalView
                .filter { !otherGlobalView.contains(it) }
                .forEach { node ->
                    val future = Patterns.ask(node, PingMessage(), CHECK_IF_ALIVE_TIMEOUT_MS)
                    val result = Await.result(future, FiniteDuration(CHECK_IF_ALIVE_TIMEOUT_MS, TimeUnit.MILLISECONDS))
                    val isAlive = result != null
                    if(isAlive) {
                        addToEventList(UUID.randomUUID() ,Event(EventEnum.STILL_ALIVE, node))
                    } else {
                        globalMayBeDead(node)
                    }
                }
    }

}