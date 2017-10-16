package partialview
import akka.actor.ActorPath

data class PartialView(var activeView: MutableSet<ActorPath> = mutableSetOf(),
                       var passiveView: MutableSet<ActorPath> = mutableSetOf())