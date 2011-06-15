package actorproto
import akka.routing._
import akka.actor.{ Actor, ActorRef }

sealed trait DiscriminatingListenerMessage
case class ListenTo(event: Event, listener: ActorRef) extends DiscriminatingListenerMessage
case class DefenTo(event: Event, listener: ActorRef) extends DiscriminatingListenerMessage

trait DiscriminatingListeners extends Listeners { self: Actor ⇒
  
}