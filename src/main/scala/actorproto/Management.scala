package actorproto

import scala.collection.immutable._
import akka.actor.Actor._
import akka.actor. {ActorRegistry, Actor, ActorRef}
import measurements.Profiling._
import akka.event.EventHandler

object Server {

  def run = {
    println("Starting the actor container...")
    Actor.remote.start("localhost", 2552)
    println("Registering the Data Collection Actor...")
    val dataCollectionActor = new DataCollectionActor
    Actor.actorOf( dataCollectionActor )
    Actor.remote.register(DataCollectionActor.serviceName, dataCollectionActor.self)
    
    println("Registering the Remediation Actor...")
    val remediationActor = new RemediationActor
    Actor.actorOf( remediationActor )
    Actor.remote.register(RemediationActor.serviceName, remediationActor.self)
    
    println("Registering the Proxy Actor for callbacks...")
    Actor.remote.register(Proxy.serviceName, Actor.actorOf(new Proxy(
      dataCollectionActor.self, 
      remediationActor.self)))
    
    println("Registering the Configuration Actor for callbacks...")
    val configurationActor = new ConfigurationActor     
    Actor.actorOf( configurationActor )
    Actor.remote.register(ConfigurationActor.serviceName, configurationActor.self)
    EventHandler.info(this, "Adding the Remediation and Data Collection Actors as Configuration Listeners...")
    configurationActor.addListener(dataCollectionActor.self)
    configurationActor.addListener(remediationActor.self)
    
    println("All actors registered and waiting for messages.")
  }

  def main(args: Array[String]) = { println("Starting messaging server..."); run }
}

object Client {
  val name = "Client: "
  val collectionMessage = "collect"
  val remediationMessage = "simple remediation"
  val MULTIPLIER = 50
  var messages = List("")
  
  def setupActorRepository = { 
    println("Starting the actor container...")
    Actor.remote.start("localhost", 2600)
  }
  
  def sendManyMessages = {
    val dataCollector = Actor.remote.actorFor(DataCollectionActor.serviceName, "localhost", 2552)
    val remediator = Actor.remote.actorFor(RemediationActor.serviceName, "localhost", 2552)
    println("Registering the Proxy Actor for callbacks...")
    Actor.remote.register(Proxy.serviceName, Actor.actorOf( new Proxy(dataCollector, remediator) ))
    val proxy = Actor.remote.actorFor(Proxy.serviceName, "localhost", 2552)
    
    var perfInfo = Map[String, Long]()
    perfInfo += "startTime" -> System.nanoTime
        
    val collectionMessages = List("collect registry", "collect")
    messages = List.tabulate(MULTIPLIER)(index => collectionMessages.apply(index % collectionMessages.length))
    // Create a List of messages by repeating the original List
    timed(printTime("Sent " + messages.length + " " + collectionMessages + " messages in ")) {
      messages foreach { message =>
        proxy ! message
      }
    }
    
    perfInfo = perfInfo + ("endTimeCollections" -> System.nanoTime)
    perfInfo = perfInfo + ("numCollections" -> messages.length)
    
    val remediationMessages = List("simple remediation", "complex remediation")
    // Create a List of messages by repeating the original List
    messages = List.tabulate(MULTIPLIER)(count => remediationMessages.apply(count % remediationMessages.length))    
    timed(printTime("Sent " + messages.length + " " + remediationMessages + " messages in ")) {
      messages foreach { message => 
        proxy ! message
      }
    }
    
    perfInfo = perfInfo + ("endTimeComplexRemediations" -> System.nanoTime)
    perfInfo = perfInfo + ("numComplexRemediations" -> messages.length)
    
    printPerformanceStats(perfInfo)
    
  }
  
  def printPerformanceStats(perfInfo : Map[String, Long]) = {
    println("------------------------------------------------------")
    println("                    Summary")
    println("------------------------------------------------------")
    println("")
    
    val collectionTime : Long = perfInfo.get("endTimeCollections").get - perfInfo.get("startTime").get
    val collectionRate : Double = perfInfo.get("numCollections").get * 1000000000 / collectionTime.toDouble
    
    val remediationTime : Long = perfInfo.get("endTimeComplexRemediations").get - perfInfo.get("endTimeCollections").get
    val remediationRate : Double = perfInfo.get("numComplexRemediations").get * 1000000000 / remediationTime.toDouble
    
    val numMessages : Long = perfInfo.get("numCollections").get + perfInfo.get("numComplexRemediations").get
    val totalTime   : Long = collectionTime + remediationTime
    val totalRate   : Double = numMessages * 1000000000 / totalTime
    
    printf("Sent %d messages at a rate of %.2f per second\n", numMessages, totalRate)
      
    println("Total time: " + formatTime(totalTime))
    
    println("\n")
    println("------------------------------------------------------")
  }
  
  def run = {
    setupActorRepository
    sendManyMessages
  }
  
  def main(args: Array[String]) =  {
    run
  }
}