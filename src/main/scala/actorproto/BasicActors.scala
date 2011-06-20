package actorproto

import akka.actor. {Actor, ActorRef}
import measurements.Profiling._

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
    case message @ "Where is Configurations?" =>
      println("Received: " + message)
      println("Reply: 2552")
      self.reply_?("2552")
    case message @ "Where is AMQPWrapper?" =>
      println("Received: " + message)
      println("Reply: 2700")
      self.reply_?("2700")
  }
}

object DirectoryActor {
    val serviceName = "directory"
}

class ConfigurationActor extends Actor {
  val name = "Configurations: "

  def receive = {
    case message @ _ =>
      println(message)
  }
}

object ConfigurationActor {
    val serviceName = "configuration"
}

class AMQPActor extends Actor {
  val name = "AMQPWrapper: "

  def receive = {
    case message @  "worker -> AMQPWrapper" =>
      println(message)
    case message @ _ =>
      AMQPWrapper.connectToAMQP(message)
  }
}

object AMQPActor{
    val serviceName = "amqp"
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

