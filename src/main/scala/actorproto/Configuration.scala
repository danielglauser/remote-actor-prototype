package actorproto

import akka.actor.Actor._
import akka.actor. {ActorRegistry, Actor, ActorRef}
import measurements.Profiling._

sealed trait ConfigSettings
case class MessageSettings(quantity: Long, messageType: String) extends ConfigSettings
case class PayloadBinding(messageToBindTo: Message, payload: String) extends ConfigSettings
case class ActorRepository( host: String) extends ConfigSettings

class ConfigurationActor() extends Actor with X, Y, Z {
  val name = "ConfigurationActor:"

  def receive = {
    case setting @ MessageSettings(quantity: Long, "collection") =>
      println(name + " Setting the max number of \"" + setting.messageType + "\" messages to " + setting.quantity) 
    case setting @ PayloadBinding(messageToBindTo: Message, payload: String) =>
      println(name + "Binding " + payload.length + " characters to the " + messageToBindTo.getClass + " message.")
    
  }
}


