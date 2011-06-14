package actorproto

import scala.collection.immutable._
import akka.actor.Actor._
import akka.actor. {ActorRegistry, Actor, ActorRef}
import measurements.Profiling._
/*
sealed trait ConfigSetting
case class MessageSetting(quantity: Long, messageType: String) extends ConfigSetting
case class PayloadBinding(messageToBindTo: Message, payload: String) extends ConfigSetting
case class ActorRepository( host: String) extends ConfigSetting

class ConfigurationActor() extends Actor {
  val name = "ConfigurationActor:"
  var settings = Map[AnyRef, ConfigSetting]()
  def receive = {
    case setting @ MessageSetting(quantity: Long, "numCollectionMessages") =>
      println(name + " Setting the max number of \"" + setting.messageType + "\" messages to " + setting.quantity)
      settings ++ setting.messageType -> setting
    case setting @ MessageSetting(quantity: Long, "numRemediationMessages") =>
      println(name + " Setting the max number of \"" + setting.messageType + "\" messages to " + setting.quantity)
      settings ++ setting.messageType -> setting
    // case MessageSetting() =>
    //   println("Getting a setting " )
    case setting @ PayloadBinding(messageToBindTo: Message, payload: String) =>
      println(name + "Binding " + payload.length + " characters to the " + messageToBindTo.getClass + " message.")
      //settings ++ (setting.messageToBindTo -> setting)
  }
}
object ConfigurationActor {
  val serviceName = "configuration-actor"
}

*/
