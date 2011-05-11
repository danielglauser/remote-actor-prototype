package actorproto

import scala.collection.immutable._
import akka.actor.Actor._
import akka.actor. {ActorRegistry, Actor, ActorRef}
import measurements.Profiling._

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
  val MULTIPLIER = 50000
  var messages = List("")
  
  //def 
  def sendManyMessages = {
    val dataCollector = Actor.remote.actorFor("data-collection-service", "localhost", 2552)
    val remediator = Actor.remote.actorFor("remediation-service", "localhost", 2552)
    //val client = Actor.actorOf( new ClientActor(dataCollector, remediator) ).start
    val client = Actor.remote.actorFor(ClientActor.serviceName, "localhost", 2552)
    
    var perfInfo = new HashMap[String, Long]
    perfInfo += "startTime" -> System.nanoTime
        
    val collectionMessages = List("collect registry", "collect")
    messages = List.tabulate(MULTIPLIER)((index: Int) => collectionMessages.apply(index % collectionMessages.length))
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
    
    printf("Sent %d collections at a rate of %.2f per second\n", perfInfo.get("numCollections").get, collectionRate)
    println("Over a total of " + formatTime(collectionTime))
    
    println("\n")
    println("------------------------------------------------------")
  }
  
  def run = sendManyMessages
  
  def main(args: Array[String]) =  {
    run
  }
}