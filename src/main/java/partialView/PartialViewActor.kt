package partialView

import AkkaConstants
import AkkaUtils
import NodeID
import akka.actor.AbstractActor
import akka.actor.Props

class PartialViewActor(myID: NodeID, contactNode: NodeID, fanout: Int): AbstractActor() {

    private var activeView: MutableSet<NodeID> = mutableSetOf()
    private var passiveView: MutableSet<NodeID> = mutableSetOf()

    companion object {
        fun props(myID: NodeID, contactNode: NodeID, fanout: Int): Props {
            return Props.create(PartialViewActor::class.java) { PartialViewActor(myID, contactNode, fanout)}
        }
    }

    init {
        val contact = AkkaUtils.lookUpRemote(context, AkkaConstants.SYSTEM_NAME, contactNode, AkkaConstants.CONTACT_NODE)
        contact.tell(AkkaConstants.JOIN_MESSAGE, self)
    }

    override fun createReceive(): Receive {
        return receiveBuilder()
                .match(String::class.java) { message ->
                    when(message) {
                        AkkaConstants.JOIN_MESSAGE -> println("recebi join message")
                        else -> {
                            println("UNKNOWN RESPONSE: $message")
                        }
                    }
                }.build()
    }
}