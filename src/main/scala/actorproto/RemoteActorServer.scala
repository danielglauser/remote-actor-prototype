package actorproto

import akka.actor.Actor._
import akka.actor. {ActorRegistry, Actor, ActorRef}
import scala.collection.immutable._
import measurements.Profiling._

class DataCollectionActor extends Actor {
  val name = "Server: "
  
  def receive = {
    case message @ "collect registry" =>
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
    case message @ "simple remediation" =>
      timed(printTime(name + " responded to \"" + message + "\" in ")) { self.reply("ACK") }
    case message @ "complex remediation" =>
      timed(printTime(name + " responded to \"" + message + "\" in ")) { self.reply("ACK") }
    case message @ _ =>
      println("Server: dropping unknown message \"" + message + "\"")
  }
}
object RemediationActor {
  val serviceName = "remediation-service"  
}

class ClientActor(dataCollector: ActorRef, remediator: ActorRef) extends Actor {
  val name = "Client:"
  def receive = {
    case message @ "collect" =>
      println(name + " Sending \"" + message + "\" -> to the " + dataCollector.id) 
      dataCollector !! message
    case message @ "collect registry" =>
      println(name + " Sending \"" + message + "\" -> to the " + dataCollector.id) 
      dataCollector !! message
    case message @ "simple remediation" =>
      println(name + " Sending \"" + message + "\" -> to the " + remediator.id)
      remediator !! message
    case message @ "complex remediation" =>
      println(name + " Sending \"" + message + "\" -> to the " + remediator.id)
      remediator !! message
    case message @ "ACK" =>
      println("Client: received ACK")
  }
}

object ClientActor { 
  val serviceName = "client"
}

object Server {

  def run = {
    println("Starting the actor container...")
    Actor.remote.start("localhost", 2552)
    println("Registering the Data Collection Actor...")
    val dataCollectionActor = Actor.actorOf( new DataCollectionActor )
    Actor.remote.register(DataCollectionActor.serviceName, dataCollectionActor)
    println("Registering the Remendiation Actor...")
    val remediationActor = Actor.actorOf( new RemediationActor )
    Actor.remote.register(RemediationActor.serviceName, remediationActor)
    println("Registering the Client Actor for callbacks...")
    Actor.remote.register(ClientActor.serviceName, Actor.actorOf( new ClientActor(dataCollectionActor, remediationActor) ))
    println("All actors registered and waiting for messages.")
  }

  def main(args: Array[String]) = { println("Starting messaging server..."); run }
}

object Client {
  val name = "Client: "
  val collectionMessage = "collect"
  val remediationMessage = "simple remediation"
  val MULTIPLIER = 10
  var messages = List("")
  
  def sendManyMessages = {
    val dataCollector = Actor.remote.actorFor("data-collection-service", "localhost", 2552)
    val remediator = Actor.remote.actorFor("remediation-service", "localhost", 2552)
    val client = Actor.actorOf( new ClientActor(dataCollector, remediator) ).start
        
    val collectionMessages = List("collect registry", "collect")
    messages = List.tabulate(MULTIPLIER)(count => collectionMessages.apply(count % collectionMessages.length))        
    timed(printTime("Sent " + messages.length + " " + collectionMessages + " messages in ")) {
      messages foreach { message =>
        client ! message
      }
    }
    
    val remediationMessages = List("simple remediation", "complex remediation")
    // Create a List of messages by repeating the original List
    messages = List.tabulate(MULTIPLIER)(count => remediationMessages.apply(count % remediationMessages.length))    
    timed(printTime("Sent " + messages.length + " " + remediationMessages + " messages in ")) {
      messages foreach { message => 
        client ! message
      }
    }
  }
  
  def run = sendManyMessages
  
  def main(args: Array[String]) =  {
    run
  }
}
