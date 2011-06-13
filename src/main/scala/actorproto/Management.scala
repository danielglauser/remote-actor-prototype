package actorproto

import scala.collection.immutable._
import akka.actor.Actor._
import akka.actor. {ActorRegistry, Actor, ActorRef}
import measurements.Profiling._
import java.util.{NoSuchElementException, Scanner}
import java.lang.{Boolean, String}

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
    Actor.remote.register(Proxy.serviceName, Actor.actorOf( new Proxy(
      Option.apply[ActorRef](dataCollectionActor),
      Option.apply[ActorRef](remediationActor),
      Option.apply[ActorRef](configurationActor))))


//    println("All actors registered and waiting for messages.")
      println("Server Listening..")
  }

  def main(args: Array[String]) = { /*println("Starting messaging server..."); */ run }
}

object Client {

  def sendCollectionMessages(destination: ActorRef, messageSettings: List[String], perfInfo: HashMap[String, Long]) = {
    var isConnected = false
    var resultingPerfInfo = perfInfo
    val collectionMessages = tabulateManyMessages(messageSettings.apply(0).toInt, List.apply[String](messageSettings.apply(2), messageSettings.apply(3)))
    resultingPerfInfo += "startTime" -> System.nanoTime
    timed(printTime("Sent " + collectionMessages.length + " messages in ")) {
      try{
        val result = (destination !! collectionMessages.head).get
        if (result == "ACK")
          isConnected = true
      }catch { case e:NoSuchElementException =>  }

    if (isConnected == false)
      println("Couldnt Establish Connection")
    else {
      collectionMessages.drop(1) foreach { message =>
        destination ! message
      }
      resultingPerfInfo += ("endTimeCollections" -> System.nanoTime)
      resultingPerfInfo += ("numCollections" -> collectionMessages.length)
    }
    }

    resultingPerfInfo
  }

  def sendRemediationMessages(destination: ActorRef, messageSettings: List[String], perfInfo: HashMap[String, Long]) = {
    var isConnected = false
    var resultingPerfInfo = perfInfo
    val remediationMessages = tabulateManyMessages(messageSettings.apply(0).toInt, List.apply[String](messageSettings.apply(4), messageSettings.apply(5)))
    timed(printTime("Sent " + remediationMessages.length + " messages in ")) {
    try{
        val result = (destination !! remediationMessages.head).get
        if (result == "ACK")
          isConnected = true
    } catch { case e:NoSuchElementException =>  }

    if(isConnected == false)
      println("In sendRemediationMessages, couldn\'t establish connection")
    else{
      remediationMessages.drop(1) foreach { message =>
      destination ! message
      }
      resultingPerfInfo += ("endTimeComplexRemediations" -> System.nanoTime)
      resultingPerfInfo += ("numComplexRemediations" -> remediationMessages.length)
    }
    }

    resultingPerfInfo

  }

  @throws(classOf[java.util.NoSuchElementException])
  def sendManyMessages = {
    val messageSettings =  userInputs
    val destination = Actor.remote.actorFor(Proxy.serviceName, "localhost", messageSettings.apply(1).toInt)
    var perfInfo = new HashMap[String, Long]
    var isConnected = false

    perfInfo = sendCollectionMessages(destination, messageSettings, perfInfo)
    perfInfo = sendRemediationMessages(destination, messageSettings, perfInfo)

    printPerformanceStats(perfInfo)
  }

  def userInputs = {
      println("Enter number of Messages to Send")
      val multiplier = (new Scanner(System.in)).next()

      println("Enter Port Number")
      val portNo = (new Scanner(System.in)).next()

      println("\nEnter first message for collectionMessages")
      val string1 = (new Scanner(System.in)).next()

      println("\nEnter second message for collectionMessages")
      val string2 = (new Scanner(System.in)).next()

      println("\nEnter first message for remediationMessages")
      val string3 = (new Scanner(System.in)).next()

      println("\nEnter second message for remediationMessages")
      val string4 = (new Scanner(System.in)).next()

      List.apply [String] (multiplier, portNo, string1, string2, string3, string4)
  }

  def tabulateManyMessages(numMessages: Int, msgString: List[String]) = {
    List.tabulate(numMessages)(index => msgString.apply(index % msgString.length))
  }

  def printPerformanceStats(perfInfo : Map[String, Long]) = {
    println("------------------------------------------------------")
    println("                    Summary")
    println("------------------------------------------------------")
    println("")

    val collectionTime : Long = perfInfo.get("endTimeCollections").getOrElse[Long](0) - perfInfo.get("startTime").getOrElse[Long](0)
    val collectionRate : Double = perfInfo.get("numCollections").getOrElse[Long](0) * 1000000000 / collectionTime.toDouble

    val remediationTime : Long = perfInfo.get("endTimeComplexRemediations").getOrElse[Long](0) - perfInfo.get("endTimeCollections").getOrElse[Long](0)
    val remediationRate : Double = perfInfo.get("numComplexRemediations").getOrElse[Long](0) * 1000000000 / remediationTime.toDouble

    val numMessages : Long = perfInfo.get("numCollections").getOrElse[Long](0) + perfInfo.get("numComplexRemediations").getOrElse[Long](0)
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