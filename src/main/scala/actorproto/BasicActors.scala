package actorproto

import akka.actor.Actor._
import akka.actor. {ActorRegistry, Actor, ActorRef}
import measurements.Profiling._
import sun.awt.SunHints.Value
import com.sun.corba.se.spi.orb.DataCollector
import scala.None

class DataCollectionActor extends Actor {
  val name = "Server: "

  def receive = {
    case message @ "collectregistry" =>
      println(name + " message from " + self.sender.get)
      timed(printTime(name + " responded to \"" + message + "\" in ")) { self.reply("ACK") }
    case message @ "collect" =>
      timed(printTime(name + " responded to \"" + message + "\" in ")) { self.reply("ACK") }
    case message @ "ACK" =>

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
    case message @ "ACK" =>

    case message @ _ =>
      println("Server: dropping unknown message \"" + message + "\"")
  }
}
object RemediationActor {
  val serviceName = "remediation-service"  
}

class Proxy(dataCollector: Option[ActorRef] = None,
            remediator: Option[ActorRef] = None,
            configuration: Option[ActorRef] = None) extends Actor {
  val name = "Proxy:"
  def receive = {
    case message @ "collect" =>
      if(dataCollector.get.isInstanceOf) {
        println("IFFFFFFFFFFFFFFFFFFFFFF")
        self.reply_?("Data Collector not set, cannot collect")
      } else {
        println("**: " + dataCollector.toString)
        println("ELSEEEEEEEEEEEEEEEEEEEEEEE")
        println(name + " Sending \"" + message + "\" -> to the " + dataCollector.get.id)
        self.reply_?("ACK")
        timed(printTime(name + " " + message + " message sent in ")) { dataCollector.get ! message }
      }
    case message @ "collectregistry" =>
      if(dataCollector.isEmpty) {
        self.reply_?("Data Collector not set, cannot collect")
      } else {
      println(name + " Sending \"" + message + "\" -> to the " + dataCollector.get.id)
      self.reply("ACK")
      timed(printTime(name + " " + message + " message sent in ")) { dataCollector.get ! message }
      }
    case message @ "simpleremediation" =>
      if(remediator.isEmpty) {
        self.reply_?("Remediator not set, cannot remediate")
      } else {
      println(name + " Sending \"" + message + "\" -> to the " + remediator.get.id)
      self.reply("ACK")
      timed(printTime(name + " " + message + " message sent in ")) { remediator.get ! message }
      }
    case message @ "complexremediation" =>
      if(remediator.isEmpty) {
        self.reply_?("Remediator not set, cannot remediate")
      } else {
      println(name + " Sending \"" + message + "\" -> to the " + remediator.get.id)
      self.reply("ACK")
      timed(printTime(name + " " + message + " message sent in ")) { remediator.get ! message }
      }
    case message @ "ACK" =>
      println("Client: received ACK")
    case message @ _ =>
      self.reply("ACK")
      println("Unknown Type: RESEND Message: " + message )
  }
}
object Proxy {
  val serviceName = "proxy"
}

