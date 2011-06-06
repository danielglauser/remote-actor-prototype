package actorproto

import scala.collection.immutable._
import akka.actor.Actor._
import akka.actor. {ActorRegistry, Actor, ActorRef}
import measurements.Profiling._
import java.lang.RuntimeException
import java.security.SignedObject
import scala.Some
import org.omg.CORBA.Any
import akka.dispatch.FutureTimeoutException

//import reflect.generic.UnPickler.Scan
import java.util.Scanner

object Server {

  def run = {
//    println("Starting the actor container...")
    Actor.remote.start("localhost", 2552)

//    println("Registering the Data Collection Actor...")
    val dataCollectionActor = Actor.actorOf( new DataCollectionActor )
    Actor.remote.register(DataCollectionActor.serviceName, dataCollectionActor)

//    println("Registering the Remendiation Actor...")
    val remediationActor = Actor.actorOf( new RemediationActor )
    Actor.remote.register(RemediationActor.serviceName, remediationActor)

//    println("Registering the Configuration Actor for callbacks...")
    val configurationActor = Actor.actorOf( new ConfigurationActor )
    Actor.remote.register(ConfigurationActor.serviceName, configurationActor)

//    println("Registering the Client Actor for callbacks...")
    Actor.remote.register(ClientActor.serviceName, Actor.actorOf( new ClientActor(dataCollectionActor, remediationActor, configurationActor) ))


//    println("All actors registered and waiting for messages.")
      println("Server Listening..")
  }

  def main(args: Array[String]) = { /*println("Starting messaging server..."); */ run }
}

object Client {
  val name = "Client: "
  val collectionMessage = "collect"
  val remediationMessage = "simple remediation"

  println("Enter number of Messages to Send")
  val MULTIPLIER = (new Scanner(System.in)).next().toInt
  var messages = List("")

  @throws(classOf[akka.dispatch.FutureTimeoutException])
  def sendManyMessages = {
//    val dataCollector = Actor.remote.actorFor(DataCollectionActor.serviceName, "localhost", 2552)
//    val remediator = Actor.remote.actorFor(RemediationActor.serviceName, "localhost", 2552)
//    val configuration = Actor.remote.actorFor(ConfigurationActor.serviceName, "localhost", 2552)
//    println("Registering the Client Actor for callbacks...")
//    Actor.remote.register(ClientActor.serviceName, Actor.actorOf( new ClientActor(dataCollector, remediator, configuration) ))

    println("Enter Port Number")
    val PortNo = (new Scanner(System.in)).next().toInt
    val client = Actor.remote.actorFor(ClientActor.serviceName, "localhost", PortNo)

    var perfInfo = new HashMap[String, Long]
    perfInfo += "startTime" -> System.nanoTime

    println("\nEnter first message for collectionMessages")
    val string1 = (new Scanner(System.in)).next()

    println("\nEnter second message for collectionMessages")
    val string2 = (new Scanner(System.in)).next()

    var result1 = "Waiting"
    val collectionMessages = List(string1, string2)
    messages = List.tabulate(MULTIPLIER)(index => collectionMessages.apply(index % collectionMessages.length))
    // Create a List of messages by repeating the original List
    timed(printTime("Sent " + messages.length + " " + collectionMessages + " messages in ")) {
      messages foreach { message =>
        try {
          val future = (client !!! message)
          result1 = future.get
        } catch {
          case e:FutureTimeoutException =>

        }
      }
    }
    if (result1 != "Ack")
      println("COULDNT ESTABLISH CONNECTION")
    else {
      perfInfo = perfInfo + ("endTimeCollections" -> System.nanoTime)
      perfInfo = perfInfo + ("numCollections" -> messages.length)
    }

      println("\nEnter first message for remediationMessages")
      val string3 = (new Scanner(System.in)).next()

      println("\nEnter second message for remediationMessages")
      val string4 = (new Scanner(System.in)).next()

    var result2 = "Waiting"
   val remediationMessages = List(string3, string4)
    // Create a List of messages by repeating the original List
    messages = List.tabulate(MULTIPLIER)(count => remediationMessages.apply(count % remediationMessages.length))
    timed(printTime("Sent " + messages.length + " " + remediationMessages + " messages in ")) {
      messages foreach { message =>
        try {
          val future = client !!! message
          result2 = future.get
      } catch {
        case e:FutureTimeoutException =>
      }
    }
  }
    if (result2 != "Ack")
      println("COULDNT ESTABLISH CONNECTION")
    else {
      perfInfo = perfInfo + ("endTimeComplexRemediations" -> System.nanoTime)
      perfInfo = perfInfo + ("numComplexRemediations" -> messages.length)

      printPerformanceStats(perfInfo)
    }


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
    sendManyMessages
  }

  def main(args: Array[String]) =  {
    run
  }
}