package actorproto
import scala.collection.immutable._
import akka.routing._
import akka.actor.{ Actor, ActorRef }

sealed trait DiscriminatingListenerMessage
case class ListenToEvent(event: SystemEvent, listener: ActorRef) extends DiscriminatingListenerMessage
case class DeafenToEvent(event: SystemEvent, listener: ActorRef) extends DiscriminatingListenerMessage

/**
 * Discriminating listeners are concerned with listening to a subset of an Actor's events.
 */
trait DiscriminatingListeners extends Listeners { self: Actor ⇒
  protected var discriminatingListeners = Map[SystemEvent, List[ActorRef]]()
  val foo = "bar"
  
  override def listenerManagement = super.listenerManagement orElse {
    case ListenToEvent(event, listener) => 
    if(discriminatingListeners.contains(event) && ) {
      if(!discriminatingListeners(event).contains(listener))
      discriminatingListeners = discriminatingListeners.updated(event, listener)
    }
    else {
      discriminatingListeners + (event -> listener)
    }
    case DeafenToEvent(event, listener) => discriminatingListeners = discriminatingListeners - event
  }
}