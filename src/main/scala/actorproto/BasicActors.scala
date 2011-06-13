package actorproto

import akka.actor.Actor._
import akka.actor. {ActorRegistry, Actor, ActorRef}
import measurements.Profiling._
import sun.awt.SunHints.Value

class DataCollectionActor extends Actor {
  val name = "Server: "

  def receive = {
    case message @ "collectregistry" =>
      println(name + " message from " + self.sender.get)
      timed(printTime(name + " responded to \"" + message + "\" in ")) { self.reply("ACK") }
    case message @ "collect" =>
      timed(printTime(name + " responded to \"" + message + "\" in ")) { self.reply("ACK") }
    case message @ _ =>
        println("Server: dropping unknown message \"" + message + "\"")
  }
}
object DataCollectionActor {
  val serviceName = "data-collection-service"
}

class RemediationActor extends Actor {
  val name = "Server: "

  def receive = {
    case message @ "simpleremediation" =>
      timed(printTime(name + " responded to \"" + message + "\" in ")) { self.reply("ACK") }
    case message @ "complexremediation" =>
      timed(printTime(name + " responded to \"" + message + "\" in ")) { self.reply("ACK") }
    case message @ _ =>
      println("Server: dropping unknown message \"" + message + "\"")
  }
}
object RemediationActor {
  val serviceName = "remediation-service"  
}

class ClientActor(dataCollector: ActorRef, remediator: ActorRef, configuration: ActorRef) extends Actor {
  val name = "Client:"
  def receive = {
    case message @ "collect" =>
      println(name + " Sending \"" + message + "\" -> to the " + dataCollector.id)
      self.reply_?("ACK")
      timed(printTime(name + " " + message + " message sent in ")) { dataCollector ! message }
    case message @ "collectregistry" =>
      println(name + " Sending \"" + message + "\" -> to the " + dataCollector.id)
      self.reply_?("ACK")
      timed(printTime(name + " " + message + " message sent in ")) { dataCollector ! message }
    case message @ "simpleremediation" =>
      println(name + " Sending \"" + message + "\" -> to the " + remediator.id)
      self.reply_?("ACK")
      timed(printTime(name + " " + message + " message sent in ")) { remediator ! message }
    case message @ "complexremediation" =>
      println(name + " Sending \"" + message + "\" -> to the " + remediator.id)
      self.reply_?("ACK")
      timed(printTime(name + " " + message + " message sent in ")) { remediator ! message }
    case message @ "ACK" =>
      self.reply_?("ACK")
      println("Client: received ACK")
    case message @ _ =>
      self.reply_?("ACK")
      println("Unknown Type: RESEND Message: " + message )
  }
}
object ClientActor { 
  val serviceName = "client"
}

