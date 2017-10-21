package systemsupervisor

import akka.actor.AbstractActor
import akka.actor.ActorRef
import akka.actor.Props
import akkanetwork.AkkaConstants
import akkanetwork.AkkaUtils
import akkanetwork.NodeID


class PicassoActor(id : NodeID) : AbstractActor() {

    companion object {
        fun props(id: NodeID): Props {
            return Props.create(PicassoActor::class.java) { PicassoActor(id)}
        }
    }

    override fun createReceive(): Receive {
        return receiveBuilder()
                .match(Any::class.java){ System.err.println("This actor shouldn't receive any message.") }
                .build()
    }
}