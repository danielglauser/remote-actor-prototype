
package actorproto

import scala.collection.immutable._
import akka.actor.Actor._
import akka.actor. {ActorRegistry, Actor, ActorRef}
import measurements.Profiling._

sealed trait ConfigSetting
case class MessageSetting(messageType: String, quantity: Long = 0) extends ConfigSetting
case class PayloadBinding(messageToBindTo: Message, payload: String) extends ConfigSetting
case class ActorRepositorySetting( host: String) extends ConfigSetting

class ConfigurationActor() extends Actor {
  val name = "ConfigurationActor:"
  // Create a mutable reference to an immutable Map.  Every "mutation" of the map requires pointing that reference
  // to the new copy of the Map.
  var settings = Map[AnyRef, ConfigSetting]()
  def receive = {
    // Retrieving settings
    case setting @ MessageSetting(messageType: String, 0) =>
      println("Getting the " + setting.messageType + " setting.")
      if (settings.contains(setting.messageType))
        self.reply(settings.apply(setting.messageType))
      else
        self.reply(name + " unknown setting - " + setting)
    // Modifying settings
    case setting @ MessageSetting("numCollectionMessages", quantity: Long) =>
      println(name + " Setting the max number of \"" + setting.messageType + "\" messages to " + setting.quantity)
      settings += setting.messageType -> setting
    case setting @ MessageSetting("numRemediationMessages", quantity: Long) =>
      println(name + " Setting the max number of \"" + setting.messageType + "\" messages to " + setting.quantity)
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
