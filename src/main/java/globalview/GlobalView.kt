package globalview

import akka.actor.ActorRef
import akka.pattern.Patterns
import akkanetwork.AkkaUtils
import globalview.GVHelpers.Companion.CHECK_IF_ALIVE_TIMEOUT_MS
import globalview.GVHelpers.Companion.MAY_BE_DEAD_PERIOD_MS
import globalview.GVHelpers.Companion.SEND_EVENTS_PERIOD_MS
import globalview.GVHelpers.Companion.SEND_HASH_PERIOD_MS
import globalview.GVHelpers.Companion.eventListisFull
import globalview.GVHelpers.Companion.pendingEventsisFull
import globalview.messages.external.ConflictMessage
import globalview.messages.external.GiveGlobalMessage
import globalview.messages.external.GlobalMessage
import globalview.messages.external.PingMessage
import globalview.messages.internal.StatusMessage
import partialview.protocols.gossip.messages.StatusMessageWrapper
import scala.concurrent.Await
import scala.concurrent.duration.FiniteDuration
import java.util.*
import java.util.concurrent.TimeUnit

class GlobalView(private val eventList: LinkedList<Pair<UUID, Event>>,
                 private val pendingEvents: MutableMap<UUID, Event>,
                 private val toRemove: MutableSet<ActorRef>,
                 private val globalView: MutableMap<ActorRef, ActorRef>,
                 private val self: ActorRef,
                 private val pvActor: ActorRef,
                 imContact: Boolean) {

    var timerSendEvents = Timer()
    val timersMayBeDead = mutableMapOf<UUID, Timer>()

    private val sendEventsTask = object : TimerTask() {
        override fun run() {
            sendEvents()
        }
    }

    init {

        if(imContact){
            globalNewNode(self, pvActor,false)
        }
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

    private fun globalAdd(globalNewNode: ActorRef, partialNewNode: ActorRef, needsGlobal: Boolean) {
        globalView.put(globalNewNode, partialNewNode)
        if (needsGlobal) {
            sendGlobalMessage(globalNewNode)
        }
    }

    fun receivedGlobalMessage(newView: MutableMap<ActorRef, ActorRef>, eventIds: LinkedList<Pair<UUID, Event>>) {
        globalView.clear()
        eventList.clear()
        globalView.putAll(newView)
        eventList.addAll(eventIds)
    }

    private fun sendGlobalMessage(node: ActorRef) {
        node.tell(GlobalMessage(globalView, eventList), self)
    }

    fun remove(node: ActorRef) {
        globalView.remove(node)
        toRemove.remove(node)
    }

    fun globalMayBeDead(globalNode: ActorRef, partialNode: ActorRef) {
        val uuid = UUID.randomUUID()
        addToEventList(uuid, Event(EventEnum.MAY_BE_DEAD, globalNode, partialNode))
        toRemove.add(globalNode)

        val timer = Timer()
        val removeNode = object : TimerTask() {
            override fun run() {
                remove(globalNode)
                timersMayBeDead.remove(uuid)
            }
        }
        timer.schedule(removeNode, MAY_BE_DEAD_PERIOD_MS)
        timersMayBeDead.put(uuid, timer)
    }

    fun globalNewNode(globalNewNode: ActorRef, partialNewNode: ActorRef, needsGlobal: Boolean) {
        val uuid = UUID.randomUUID()
        addToEventList(uuid, Event(EventEnum.NEW_NODE, globalNewNode, partialNewNode))
        globalAdd(globalNewNode, partialNewNode , needsGlobal)
    }

    private fun addToEventList(eventId: UUID, event: Event) {
        if(pendingEvents.isEmpty()) {
            timerSendEvents = Timer()
            timerSendEvents.schedule(sendEventsTask, SEND_EVENTS_PERIOD_MS)
        } else if(event.globalNode == self && event.event == EventEnum.MAY_BE_DEAD) {
            timerSendEvents.cancel()
            timerSendEvents.schedule(sendEventsTask, SEND_EVENTS_PERIOD_MS)
        }

        if (eventListisFull(eventList)) {
            eventList.removeFirst()
        }
        eventList.add(Pair(eventId, event))
        pendingEvents.put(eventId, event)

        if(pendingEventsisFull(pendingEvents)) {
            timerSendEvents.cancel()
            globalBroadcast()
        }
    }

    private fun imAlive() {
        addToEventList(UUID.randomUUID(), Event(EventEnum.STILL_ALIVE, self, pvActor))
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
        gottaGoFastSet.addAll(eventList.map { it.first })


        newEvents.forEach {
            val removeNodeTask = object : TimerTask() {
                override fun run() {
                    remove(it.value.globalNode)
                    timersMayBeDead.remove(it.key)
                }
            }

            if(!gottaGoFastSet.contains(it.key)) {
                addToEventList(it.key, it.value)
                val type = it.value.event
                val globalNode = it.value.globalNode
                val partialNode = it.value.partialNode
                if (type == EventEnum.NEW_NODE) {
                    globalAdd(globalNode, partialNode,false)
                } else if (type == EventEnum.MAY_BE_DEAD) {
                    if(globalNode != self) {
                        toRemove.add(globalNode)
                        val timer = Timer()
                        timer.schedule(removeNodeTask, MAY_BE_DEAD_PERIOD_MS)
                        timersMayBeDead.put(it.key, timer)
                    } else {
                      imAlive()
                    }
                } else if (type == EventEnum.STILL_ALIVE) {
                    if(toRemove.contains(globalNode)) {
                        toRemove.remove(globalNode)
                        timersMayBeDead.remove(it.key)?.cancel()
                    } else {
                        globalAdd(it.value.globalNode, partialNode, false)
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

    fun conflictMessageReceived(otherGlobalView: MutableMap<ActorRef, ActorRef>) {
        otherGlobalView
                .filter { !globalView.containsKey(it.key) }
                .forEach { entry ->
                    var isAlive = false
                    try {
                        val future = Patterns.ask(entry.key, PingMessage(), CHECK_IF_ALIVE_TIMEOUT_MS)
                        isAlive = Await.result(future, FiniteDuration(CHECK_IF_ALIVE_TIMEOUT_MS, TimeUnit.MILLISECONDS)) as Boolean
                    } catch (e: Exception) { }
                    if(isAlive) {
                        globalNewNode(entry.key, entry.value, false)
                    } else {
                        addToEventList(UUID.randomUUID(), Event(EventEnum.MAY_BE_DEAD, entry.key, entry.value))
                    }
                }
        globalView
                .filter { !otherGlobalView.containsKey(it.key) }
                .forEach { entry ->
                    var isAlive = false
                    try {
                        val future = Patterns.ask(entry.key, PingMessage(), CHECK_IF_ALIVE_TIMEOUT_MS)
                        isAlive = Await.result(future, FiniteDuration(CHECK_IF_ALIVE_TIMEOUT_MS, TimeUnit.MILLISECONDS)) as Boolean
                    } catch (e: Exception) { }
                    if(isAlive) {
                        addToEventList(UUID.randomUUID() ,Event(EventEnum.STILL_ALIVE, entry.key, entry.value))
                    } else {
                        globalMayBeDead(entry.key, entry.value)
                    }
                }
    }

    fun partialNodeMayBeDead(partialNode: ActorRef) {
        val globalNode = globalView.filterValues { it == partialNode }.entries.first().key
        globalMayBeDead(globalNode, partialNode)
    }

}