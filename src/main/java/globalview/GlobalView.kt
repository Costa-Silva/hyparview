package globalview

import akka.actor.ActorRef
import globalview.GVHelpers.Companion.MAY_BE_DEAD_PERIOD_MS
import globalview.GVHelpers.Companion.SEND_EVENTS_PERIOD_MS
import globalview.GVHelpers.Companion.SEND_HASH_PERIOD_MS
import globalview.GVHelpers.Companion.eventListisFull
import globalview.GVHelpers.Companion.pendingEventsisFull
import globalview.messages.GlobalMessage
import globalview.messages.StatusMessage
import partialview.protocols.gossip.messages.StatusMessageWrapper
import java.util.*

class GlobalView(private val eventList: LinkedList<UUID>,
                 private val pendingEvents: MutableMap<UUID, Event>,
                 private val toRemove: MutableSet<ActorRef>,
                 private val globalView: MutableSet<ActorRef>,
                 private val pvActor: ActorRef,
                 val self: ActorRef) {

    val timerSendEvents = Timer()
    val timersMayBeDead = mutableMapOf<UUID, Timer>()

    val sendEventsTask = object : TimerTask() {
        override fun run() {
            sendEvents()
        }
    }

    init {
        val sendHash = object : TimerTask() {
            override fun run() {
                timerSendEvents.cancel()
                globalBroadcast()
            }
        }
        Timer().schedule(sendHash ,0, SEND_HASH_PERIOD_MS)
    }

    fun sendEvents() {
        globalBroadcast()
    }

    fun globalBroadcast() {
        val message = StatusMessage(globalView.hashCode(), pendingEvents, toRemove.isEmpty())
        pvActor.tell(StatusMessageWrapper(message, self), ActorRef.noSender())
    }

    fun globalAdd(newNode: ActorRef, needsGlobal: Boolean) {
        globalView.add(newNode)
        if (needsGlobal) {
            sendGlobalMessage(newNode)
        }
    }

    fun receivedGlobalMessage(newView: MutableSet<ActorRef>, eventIds: MutableSet<UUID>) {
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

    fun globalNewNode(newNode: ActorRef, needsGlobal: Boolean) {
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

    fun imAlive() {
        addToEventList(UUID.randomUUID(), Event(EventEnum.STILL_ALIVE, self))
    }

    fun partialDeliver(message: StatusMessageWrapper) {
        val status = message.statusMessage
        val senderID = message.sender
    }
}