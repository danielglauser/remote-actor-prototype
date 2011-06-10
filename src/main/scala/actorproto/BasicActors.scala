package actorproto

import akka.actor.Actor._
import akka.actor. {ActorRegistry, Actor, ActorRef}
import measurements.Profiling._

class DataCollectionActor extends Actor {
  val name = "Server: "
  
  def receive = {
    case message @ "collect registry" =>
      println(name + " message from " + self.sender.get)
      //timed(printTime(name + " responded to \"" + message + "\" in ")) { self.reply("ACK") }
    case message @ "collect" =>
      //timed(printTime(name + " responded to \"" + message + "\" in ")) { self.reply("ACK") }
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
    case message @ "simple remediation" =>
      //timed(printTime(name + " responded to \"" + message + "\" in ")) { self.reply("ACK") }
    case message @ "complex remediation" =>
      //timed(printTime(name + " responded to \"" + message + "\" in ")) { self.reply("ACK") }
    case message @ _ =>
      println("Server: dropping unknown message \"" + message + "\"")
  }
}
object RemediationActor {
  val serviceName = "remediation-service"  
}

class Proxy(dataCollector: ActorRef, remediator: ActorRef) extends Actor {
  val name = "Proxy:"
  def receive = {
    case message @ "collect" =>
      println(name + " Sending \"" + message + "\" -> to the " + dataCollector.id) 
      timed(printTime(name + " " + message + " message sent in ")) { dataCollector ! message }
    case message @ "collect registry" =>
      println(name + " Sending \"" + message + "\" -> to the " + dataCollector.id) 
      timed(printTime(name + " " + message + " message sent in ")) { dataCollector ! message }
    case message @ "simple remediation" =>
      println(name + " Sending \"" + message + "\" -> to the " + remediator.id)
      timed(printTime(name + " " + message + " message sent in ")) { remediator ! message }
    case message @ "complex remediation" =>
      println(name + " Sending \"" + message + "\" -> to the " + remediator.id)
      timed(printTime(name + " " + message + " message sent in ")) { remediator ! message }
    case message @ "ACK" =>
      println("Client: received ACK")
  }
}
object Proxy { 
  val serviceName = "proxy"
}

