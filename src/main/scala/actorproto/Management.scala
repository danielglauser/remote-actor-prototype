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
      val numberOfMsgs = new Scanner(System.in).next()

      println("Enter Key to Authenticate")
      val secretKey = new Scanner(System.in).next()

      (message, numberOfMsgs, secretKey)

  }

  def startWorker = {

    val workerPort = Config.config.getInt("project-name.workerPort").get
    val workerHost = Config.config.getString("project-name.workerHost").get
    println("Starting the worker on " + workerPort)
    Actor.remote.start(workerHost, workerPort)
    Actor.remote.register(WorkerActor.serviceName, Actor.actorOf(new WorkerActor))
  }

  def connectToDirectory1(secretKey: String) = {
    val directoryPort = Config.config.getInt("project-name.directoryPort").get
    val directoryHost = Config.config.getString("project-name.directoryHost").get
    val directoryDest = Actor.remote.actorFor(DirectoryActor.serviceName, directoryHost, directoryPort)

    val configurationPort = (directoryDest !! "Where is Configurations? "+secretKey).get

    if(configurationPort.toString == "0000"){
      println("INVALID KEY")
      sys.exit(0)
    }

    configurationPort.toString.toInt
  }

  def connectToConfigurations(configurationPort: Int) = {
    val configurationDest = Actor.remote.actorFor(ConfigurationActor.serviceName, "localhost", configurationPort)
    configurationDest ! "worker -> Configuration"
  }

  def connectToDirectory2(secretKey:String) = {
    val directoryPort = Config.config.getInt("project-name.directoryPort").get
    val directoryDest = Actor.remote.actorFor(DirectoryActor.serviceName, "localhost", directoryPort)

    val AMQPPort = (directoryDest !! "Where is AMQPWrapper? "+secretKey).get

    AMQPPort.toString.toInt
  }

  def connectToAMQP(AMQPPort: Int, message: String, numberOfMsgs: String, secretKey: String) = {
    val AMQPDest = Actor.remote.actorFor(AMQPActor.serviceName, "localhost", AMQPPort)
    AMQPDest ! "worker -> AMQPWrapper"
    AMQPDest ! (message + "#" + numberOfMsgs + "`" + secretKey)
  }

  def run = {
    startWorker
    val (message, numberOfMsgs, secretKey) = msgDetails
    val configurationPort = connectToDirectory1(secretKey)
    connectToConfigurations(configurationPort)
    val AMQPPort = connectToDirectory2(secretKey)
    connectToAMQP(AMQPPort, message, numberOfMsgs, secretKey)
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

  def getKey = {
      println("Enter Key to Authenticate")
      val secretKey = new Scanner(System.in).next()

      secretKey
  }

  def start = {
    println("Starting the Configurations on 2700")
    Actor.remote.start("localhost", 2700)
    Actor.remote.register(AMQPActor.serviceName, Actor.actorOf(new AMQPActor))
  }

    def connectToDirectory1(secretKey: String) = {
    val directoryPort = Config.config.getInt("project-name.directoryPort").get
    val directoryDest = Actor.remote.actorFor(DirectoryActor.serviceName, "localhost", directoryPort)

    val configurationPort = (directoryDest !! "Where is Configurations? "+secretKey).get

    if(configurationPort.toString == "0000"){
      println("INVALID KEY")
      sys.exit(0)
    }
    configurationPort.toString.toInt
  }

  def connectToConfigurations(configurationPort: Int) = {
    val configurationDest = Actor.remote.actorFor(ConfigurationActor.serviceName, "localhost", configurationPort)
    configurationDest ! "AMQPWrapper -> Configuration"
  }

  def connectToAMQP(message: Any) = {

      val (messageToSend, numberOfMsg, key) = msgManipulations(message)

      val connection = AMQP. newConnection()

      val exchangeParameters = ExchangeParameters("hello")
      val producer = AMQP.newProducer(connection, ProducerParameters(Some(exchangeParameters), producerId = Some("my_producer")))


      for (i <- 1 to numberOfMsg)
        producer ! Message(messageToSend.getBytes, key)
  }

  def msgManipulations(message: Any) = {
      val messageString = message.toString
      val index1 =  messageString.indexOf("#")
      val index2 = messageString.indexOf("`")
      val msgPart = messageString.slice(0, index1)
      val numberOfMsg = messageString.slice(index1+1, index2)
      val key = messageString.slice(index2+1, messageString.length())

//    Or any other processing required
      val messageToSend = msgPart.reverse

      (messageToSend, numberOfMsg.toInt, key)
  }

  def main(args: Array[String]) {
    start
    val secretKey = getKey
    val configurationPort = connectToDirectory1(secretKey)
    connectToConfigurations(configurationPort)
  }
}

object Consumer {

  def start = {

    val connection = AMQP. newConnection()

    val secretKey = Config.config.getString("project-name.secretKey").get
    val exchangeParameters = ExchangeParameters("hello")
    val myConsumer = AMQP.newConsumer(connection, ConsumerParameters(secretKey, actorOf(new ConsumerActor), None, Some(exchangeParameters)))
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
