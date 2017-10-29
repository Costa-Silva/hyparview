package partialview.protocols.entropy

import akka.actor.AbstractActor
import akka.actor.ActorRef
import akka.actor.Props
import akkanetwork.AkkaConstants.Companion.PARTIAL_ACTOR
import akkanetwork.AkkaUtils
import akkanetwork.NodeID
import partialview.protocols.entropy.messages.CutTheWireMessage
import partialview.protocols.entropy.messages.KillMessage

class EntropyActor(option: EntropyOptions, arguments: Array<NodeID>): AbstractActor() {

    companion object {
        fun props(option: EntropyOptions, arguments: Array<NodeID>): Props {
            return Props.create(EntropyActor::class.java) { EntropyActor(option, arguments) }
        }
    }

    init {
        when(option) {
            EntropyOptions.CUT_WIRE -> cutWire(arguments[0], arguments[1])
            EntropyOptions.KILL -> kill(arguments[0])
        }
    }

    private fun cutWire(actor1ID: NodeID, actor2ID: NodeID) {
        val actorSelect1 = AkkaUtils.lookUpRemote(context, actor1ID, PARTIAL_ACTOR)
        val actorSelect2 = AkkaUtils.lookUpRemote(context, actor2ID, PARTIAL_ACTOR)
        actorSelect1.tell(CutTheWireMessage(actor2ID.identifier), context.self())
        actorSelect2.tell(CutTheWireMessage(actor1ID.identifier), context.self())
    }

    private fun kill(actorID: NodeID) {
        val actor = AkkaUtils.lookUpRemote(context, actorID, PARTIAL_ACTOR)
        actor.tell(KillMessage(), ActorRef.noSender())
    }


    override fun createReceive(): Receive {
        return receiveBuilder()
                .match(Any::class.java){ System.err.println("This actor shouldn't receive any message.") }
                .build()
    }
}