package actorproto

import akka.actor.Actor._
import akka.actor. {ActorRegistry, Actor, ActorRef}
import measurements.Profiling._
import sun.awt.SunHints.Value
import java.util.jar.Attributes.Name

class WorkerActor extends Actor {
  val name = "Worker: "

  def receive = {
    case message @ _ =>
      println("In workerActor receive")
  }
}

object WorkerActor {
    val serviceName = "worker"
}

class DirectoryActor extends Actor {
  val name = "Directory: "

  def receive = {
    case message @ _ =>
      println("In directoryActor receive:" + message)
  }
}

object DirectoryActor {
    val serviceName = "directory"
}




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

class Proxy(dataCollector: Option[ActorRef] =  None, remediator: Option[ActorRef] = None, configuration: Option[ActorRef] = None ) extends Actor {
  val name = "Proxy:"
  def receive = {
    case message @ "collect" =>
      println(name + " Sending \"" + message + "\" -> to the " + dataCollector.get.id)
      self.reply_?("ACK")
      timed(printTime(name + " " + message + " message sent in ")) { dataCollector.get ! message }
    case message @ "collectregistry" =>
      println(name + " Sending \"" + message + "\" -> to the " + dataCollector.get.id)
      self.reply_?("ACK")
      timed(printTime(name + " " + message + " message sent in ")) { dataCollector.get ! message }
    case message @ "simpleremediation" =>
      println(name + " Sending \"" + message + "\" -> to the " + remediator.get.id)
      self.reply_?("ACK")
      timed(printTime(name + " " + message + " message sent in ")) { remediator.get ! message }
    case message @ "complexremediation" =>
      println(name + " Sending \"" + message + "\" -> to the " + remediator.get.id)
      self.reply_?("ACK")
      timed(printTime(name + " " + message + " message sent in ")) { remediator.get ! message }
    case message @ "ACK" =>
      self.reply_?("ACK")
      println("Client: received ACK")
    case message @ _ =>
      self.reply_?("ACK")
      println("Unknown Type: RESEND Message: " + message )
  }
}
object Proxy {
  val serviceName = "proxy"
}

