package actorproto

import scala.collection.immutable._
import akka.actor.Actor._
import akka.actor. {ActorRegistry, Actor, ActorRef}
import measurements.Profiling._

sealed trait ConfigSetting
case class MessageSetting(messageType: String, quantity: Option[Long] = None) extends ConfigSetting
case class PayloadBinding(messageToBindTo: Message, payload: String) extends ConfigSetting
case class ActorRepositorySetting( host: String) extends ConfigSetting

sealed trait Event
sealed trait ConfigEvents extends Event
case class ConfigSettingChanged(before: ConfigSetting, after: ConfigSetting) extends ConfigEvents

class ConfigurationActor() extends Actor {
  val name = "ConfigurationActor:"
  // Create a mutable reference to an immutable Map.  Every "mutation" of the map requires pointing that reference
  // to the new copy of the Map.
  var settings = Map[AnyRef, ConfigSetting]()
  // A datastructure of listeners for events.  The key is an Event and the value is a list of Actors to call back
  // when that event triggers
  var listeners = Map[Event, List[AnyRef]]()
  
  def receive = {
    // Retrieving settings
    case setting @ MessageSetting(messageType: String, None) =>
      println("Getting the " + setting.messageType + " setting.")
      if (settings.contains(setting.messageType))
        self.reply(settings.apply(setting.messageType))
      else
        self.reply(name + " unknown setting - " + setting)
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
  val ACK = "Setting accepted"
  val usage = "The " + serviceName + " responds to the following messages: "
}


