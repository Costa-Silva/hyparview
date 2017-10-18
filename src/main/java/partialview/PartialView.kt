package partialview
import akka.actor.ActorContext
import akka.actor.ActorPath
import akka.actor.ActorRef
import akkanetwork.AkkaUtils
import partialview.messages.DisconnectMessage

data class PartialView(var activeView: MutableSet<ActorPath> = mutableSetOf(),
                       var passiveView: MutableSet<ActorPath> = mutableSetOf()){

    fun addNodeActiveView(nodePath: ActorPath, self: ActorRef, context: ActorContext) {
        if(nodePath != self.path() && !activeView.contains(nodePath)) {
            if(PVHelpers.activeViewisFull(activeView)) {
                dropRandomElementFromActiveView(context, self)
            }
            activeView.add(nodePath)
        }
    }
    fun addNodePassiveView(nodePath: ActorPath, self: ActorRef) {
        if(nodePath != self.path() && !activeView.contains(nodePath) &&
                !passiveView.contains(nodePath)) {
            if(PVHelpers.passiveViewisFull(passiveView)) {
                passiveView.remove(AkkaUtils.chooseRandom(passiveView))
            }
            passiveView.add(nodePath)
        }
    }

    private fun dropRandomElementFromActiveView(context: ActorContext, self: ActorRef) {
        val nodePath = AkkaUtils.chooseRandom(activeView)
        val actor = context.actorSelection(nodePath)
        actor.tell(DisconnectMessage(), self)
        activeView.remove(nodePath)
        passiveView.add(nodePath)
    }
}