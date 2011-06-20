package actorproto

import scala.collection.immutable._
import akka.actor.Actor._
import akka.actor. {ActorRegistry, Actor, ActorRef, Actors, UntypedActor}
import measurements.Profiling._
import java.lang.{Boolean, String}
import java.io.{BufferedInputStream, FileInputStream, File}
import java.util.{StringTokenizer, Properties, NoSuchElementException, Scanner}
import akka.config._
import akka.amqp._
import akka.amqp.AMQP._
import com.rabbitmq.client.Address
import java.util.concurrent.{TimeUnit, CountDownLatch}
import util.Random

object Worker {

  def msgDetails = {
      println("Enter Message to Send")
      val message = new Scanner(System.in).next()

      println("Enter Number of Messages")
      val numberOfMsgs = new Scanner(System.in).next().toInt

      (message, numberOfMsgs)

  }

  def startWorker = {

    val workerPort = Config.config.getInt("project-name.workerPort").get
    val workerHost = Config.config.getString("project-name.workerHost").get
    println("Starting the worker on " + workerPort)
    Actor.remote.start(workerHost, workerPort)
    Actor.remote.register(WorkerActor.serviceName, Actor.actorOf(new WorkerActor))
  }

  def connectToDirectory1 = {
    val directoryPort = Config.config.getInt("project-name.directoryPort").get
    val directoryHost = Config.config.getString("project-name.directoryHost").get
    val directoryDest = Actor.remote.actorFor(DirectoryActor.serviceName, directoryHost, directoryPort)

    val configurationPort = (directoryDest !! "Where is Configurations?").get

    configurationPort.toString.toInt
  }

  def connectToConfigurations(configurationPort: Int) = {
    val configurationDest = Actor.remote.actorFor(ConfigurationActor.serviceName, "localhost", configurationPort)
    configurationDest ! "worker -> Configuration"
  }


  def connectToDirectory2 = {
    val directoryPort = Config.config.getInt("project-name.directoryPort").get
    val directoryDest = Actor.remote.actorFor(DirectoryActor.serviceName, "localhost", directoryPort)

    val AMQPPort = (directoryDest !! "Where is AMQPWrapper?").get

    AMQPPort.toString.toInt
  }

  def connectToAMQP(AMQPPort: Int, message: String, numberOfMsgs: Int) = {
    val AMQPDest = Actor.remote.actorFor(AMQPActor.serviceName, "localhost", AMQPPort)
    AMQPDest ! "worker -> AMQPWrapper"
    for( i <- 1 to numberOfMsgs)
      AMQPDest ! message
  }

  def run = {
    startWorker
    val (message, numberOfMsgs) = msgDetails
    val configurationPort = connectToDirectory1
    connectToConfigurations(configurationPort)
    val AMQPPort = connectToDirectory2
    connectToAMQP(AMQPPort, message, numberOfMsgs)
  }

  def main(args: Array[String]) {
    run
  }
}

object Directory {

  def start = {
    val directoryPort = Config.config.getInt("project-name.directoryPort").get
    println("Starting the directory on " + directoryPort)
    Actor.remote.start("localhost", directoryPort)
    Actor.remote.register(DirectoryActor.serviceName, Actor.actorOf(new DirectoryActor))
  }

  def main(args: Array[String]) {
    start
  }
}

object Configurations {

  def start = {
    println("Starting the Configurations on 2552")
    Actor.remote.start("localhost", 2552)
    Actor.remote.register(ConfigurationActor.serviceName, Actor.actorOf(new ConfigurationActor))
  }

  def main(args: Array[String]) {
    start
  }
}

object AMQPWrapper {

  def start = {
    println("Starting the Configurations on 2700")
    Actor.remote.start("localhost", 2700)
    Actor.remote.register(AMQPActor.serviceName, Actor.actorOf(new AMQPActor))
  }

    def connectToDirectory1 = {
    val directoryPort = Config.config.getInt("project-name.directoryPort").get
    val directoryDest = Actor.remote.actorFor(DirectoryActor.serviceName, "localhost", directoryPort)

    val configurationPort = (directoryDest !! "Where is Configurations?").get

    configurationPort.toString.toInt
  }

  def connectToConfigurations(configurationPort: Int) = {
    val configurationDest = Actor.remote.actorFor(ConfigurationActor.serviceName, "localhost", configurationPort)
    configurationDest ! "AMQPWrapper -> Configuration"
  }

  def connectToAMQP(message: Any) = {

      val connection = AMQP. newConnection()

      val exchangeParameters = ExchangeParameters("hello")
      val producer = AMQP.newProducer(connection, ProducerParameters(Some(exchangeParameters), producerId = Some("my_producer")))

      producer ! Message(message.toString.getBytes, "some.routing.key")
  }

  def main(args: Array[String]) {
    start
    val configurationPort = connectToDirectory1
    connectToConfigurations(configurationPort)
  }
}

object Consumer {

  def start = {

    val connection = AMQP. newConnection()

    val exchangeParameters = ExchangeParameters("hello")

    val myConsumer = AMQP.newConsumer(connection, ConsumerParameters("some.routing.key", actorOf(new Actor { def receive = {
      case Delivery(payload, _, _, _, _, _) =>
        println("Received from Worker: " + new String(payload))
    }}), None, Some(exchangeParameters)))
  }

  def main(args: Array[String]) {
    start
  }
}

object LoadBalancingDemo {

  def main(args: Array[String]) {

    val workers = 10
    val messages = 10
    val maxRandomWaitMs = 5000

    val myAddresses = Array(new Address("localhost", 5672))
    val connectionParameters = ConnectionParameters(myAddresses, "guest", "guest", "localhost")
    val localConnection = AMQP.newConnection(connectionParameters)
    val directExchangeParameters = ExchangeParameters("amqp.direct", Direct)
    // specifies how many messages the amqp channel
    // should
    // prefetch as unacknowledged messages before processing
    // 0 = unlimited
    val smallPrefetchChannelParameters = Some(ChannelParameters(prefetchSize = 1))
    val someRoutingKey = "some.routing.key"

    val countDownLatch = new CountDownLatch(messages)


    // consumer
    class JobConsumer(id: Int) extends Actor {
      self.id = "jobconsumer-" + id

      def receive = {
        case Delivery(payload, _, _, _, _, _) =>
          println(self.id + " received message: " + new String(payload))
          TimeUnit.MILLISECONDS.sleep(Random.nextInt(maxRandomWaitMs))
          countDownLatch.countDown
      }
    }

    // consumers
    for (i <- 1 to workers) {
      val actor =
        AMQP.newConsumer(
          connection = localConnection,
          consumerParameters = ConsumerParameters(
            routingKey = someRoutingKey,
            deliveryHandler = Actor.actorOf(new JobConsumer(i)),
            queueName = Some("my-job-queue"),
            exchangeParameters = Some(directExchangeParameters),
            channelParameters = smallPrefetchChannelParameters))
    }

    // producer
    val producer = AMQP.newProducer(
      connection = localConnection,
      producerParameters = ProducerParameters(
        exchangeParameters = Some(directExchangeParameters)))

    //
    for (i <- 1 to messages) {
      producer ! Message(("data (" + i + ")").getBytes, someRoutingKey)
    }
    println("Sent all " + messages + " messages - awaiting processing...")

    countDownLatch.await((maxRandomWaitMs * messages) + 1000, TimeUnit.MILLISECONDS)

    AMQP.shutdownAll
    Actor.registry.shutdownAll
  }
}













/*

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

//    println("Registering the Configurations Actor for callbacks...")
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

object Parser{

  def readFile = {

    var count = 0

    println("Enter name of Config File")
    val configFile = (new Scanner(System.in)).next()

    val myFile = new File(configFile)
    val fis = new FileInputStream(myFile);
	  val bis = new BufferedInputStream(fis);

    var p = new Properties();
	  p.load(bis)

    val st = new StringTokenizer(p.getProperty("abcd"));

    var data = new HashMap[Int, String]

    while (st.hasMoreTokens()) {
      data += (count -> st.nextToken())
      count+1
    }

    data.get(0).get.toInt
  }

  def main(args: Array[String]) {
    readFile
  }
} */