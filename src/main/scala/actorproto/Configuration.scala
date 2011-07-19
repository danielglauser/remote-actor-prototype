package actorproto

import scala.collection.immutable._
import akka.actor.Actor._
import akka.actor. {ActorRegistry, Actor, ActorRef}
import measurements.Profiling._
import akka.routing._
import akka.camel.{Ack, Failure}
import akka.util.ListenerManagement

sealed trait ConfigSetting
case class MessageSetting(messageType: String, quantity: Option[Long] = None) extends ConfigSetting
case class PayloadBinding(messageToBindTo: Message, payload: String) extends ConfigSetting
case class ActorRepositorySetting( host: String) extends ConfigSetting

sealed trait SystemEvent
sealed trait ConfigEvents extends SystemEvent
case class ConfigSettingChanged(before: ConfigSetting, after: ConfigSetting) extends ConfigEvents

class ConfigurationActor() extends Actor with Listeners with ListenerManagement {
  val name = "ConfigurationActor:"
  // Create a mutable reference to an immutable Map.  Every "mutation" of the map requires pointing that reference
  // to the new copy of the Map.
  protected var settings = Map[AnyRef, ConfigSetting]()
  
  def receive = {
    // Retrieving settings
    case setting @ MessageSetting(messageType: String, None) =>
      println("Getting the " + setting.messageType + " setting.")
      if (settings.contains(setting.messageType))
        self.reply_?(settings.apply(setting.messageType))
      else
        self.reply_?(None)
        
    // Modifying settings (Mutation)
    case setting @ MessageSetting("numCollectionMessages", quantity) =>
      println(name + " Setting the max number of \"" + setting.messageType + "\" messages to " + 
        setting.quantity.getOrElse("UNKNOWN"))
      settings += setting.messageType -> setting
      
      
    case setting @ MessageSetting("numRemediationMessages", quantity) =>
      println(name + " Setting the max number of \"" + setting.messageType + "\" messages to " + 
        setting.quantity.getOrElse("UNKNOWN"))
      settings += setting.messageType -> setting
      
    // For managing message payload size
    case setting @ PayloadBinding(messageToBindTo: Message, payload: String) =>
      println(name + "Binding " + payload.length + " characters to the " + messageToBindTo.getClass + " message.")
      settings += setting.messageToBindTo -> setting
    
    case "help" => ConfigurationActor.usage
    
  }
}
object ConfigurationActor {
  val serviceName = "configuration-actor"
  val usage = "The " + serviceName + " responds to the following messages: "
}


