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
  val MULTIPLIER = 100
  var messages = List("")
  
  def sendManyMessages = {
    val dataCollector = Actor.remote.actorFor("data-collection-service", "localhost", 2552)
    val remediator = Actor.remote.actorFor("remediation-service", "localhost", 2552)
    val client = Actor.actorOf( new ClientActor(dataCollector, remediator) ).start
    
    var perfInfo: Map[String, Long] = Map("startTime" -> System.nanoTime)
        
    val collectionMessages = List("collect registry", "collect")
    messages = List.tabulate(MULTIPLIER)(count => collectionMessages.apply(count % collectionMessages.length))        
    timed(printTime("Sent " + messages.length + " " + collectionMessages + " messages in ")) {
      messages foreach { message =>
        client ! message
      }
    }
    
    perfInfo = perfInfo + ("endTimeCollections" -> System.nanoTime)
    perfInfo = perfInfo + ("numCollections" -> messages.length)
    
    val remediationMessages = List("simple remediation", "complex remediation")
    // Create a List of messages by repeating the original List
    messages = List.tabulate(MULTIPLIER)(count => remediationMessages.apply(count % remediationMessages.length))    
    timed(printTime("Sent " + messages.length + " " + remediationMessages + " messages in ")) {
      messages foreach { message => 
        client ! message
      }
    }
    
    perfInfo = perfInfo + ("endTimeComplexRemediations" -> System.nanoTime)
    perfInfo = perfInfo + ("numComplexRemediations" -> messages.length)
    
    // Otherwise this prints out too soon since the IO for the actors happens in a different thread
    Thread.sleep(1500)
    printPerformanceStats(perfInfo)
    
    // Need to figure out how to wait of all the threads to complete
    // Need to figure out why the ACKs don't show up
  }
  
  def printPerformanceStats(perfInfo : Map[String, Long]) = {
    println("------------------------------------------------------")
    println("                    Summary")
    println("------------------------------------------------------")
    println("")
    
    val collectionTime : Long = perfInfo.get("endTimeCollections").get - perfInfo.get("startTime").get
    val collectionRate : Double = perfInfo.get("numCollections").get / (collectionTime.toDouble / 1000000000)
    
    println("Sent " + perfInfo.get("numCollections").get + " collections at a rate of " + collectionRate + " per second")
    println("Over a total of " + (collectionTime/1000000).toDouble + " milliseconds")
    
    println("\n")
    println("------------------------------------------------------")
  }
  
  def run = sendManyMessages
  
  def main(args: Array[String]) =  {
    run
  }
}
