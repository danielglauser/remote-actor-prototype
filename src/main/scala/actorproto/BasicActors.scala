package actorproto

import akka.amqp._
import akka.config.Config
import java.lang.String
import akka.actor.Actor

case class entireMessage(workerNumber: Int, messageString: String, secretKey: String)
case class allExceptWorkerNumber(messageString: String, secretKey: String)

class WorkDistributorActor extends Actor {
  val name = "WorkDistributor: "

  def receive = {
    case message @ _ =>
      println("In WorkDistributorActor receive")
  }
}

object WorkDistributorActor {
    val serviceName = "workDistributor"
}

class Worker1Actor extends Actor {
  val name = "WorkerService: "
  def receive = {
    case entireMessage(1,messageString,secretKey) =>
      WorkerService.run(messageString, secretKey)
  }
}
object Worker1Actor {
    val serviceName = "worker1"
}

class Worker2Actor extends Actor {
  val name = "WorkerService: "
  def receive = {
    case entireMessage(2,messageString, secretKey) =>
      WorkerService.run(messageString, secretKey)
  }
}
object Worker2Actor {
    val serviceName = "worker2"
}

class Worker3Actor extends Actor {
  val name = "WorkerService: "
  def receive = {
    case entireMessage(3,messageString,secretKey) =>
      WorkerService.run(messageString, secretKey)
  }
}
object Worker3Actor {
    val serviceName = "worker3"
}

class Worker4Actor extends Actor {
  val name = "WorkerService: "
  def receive = {
    case entireMessage(4,messageString,secretKey) =>
      WorkerService.run(messageString, secretKey)
  }
}
object Worker4Actor {
    val serviceName = "worker4"
}

class Worker5Actor extends Actor {
  val name = "WorkerService: "
  def receive = {
    case entireMessage(5,messageString, secretKey) =>
      WorkerService.run(messageString, secretKey)
  }
}
object Worker5Actor {
    val serviceName = "worker5"
}

class DirectoryActor extends Actor {
  val name = "Directory: "
  val secretKey = Config.config.getString("project-name.secretKey").get
  val CONFMessage = "Where is Configurations? " + secretKey
  val AMQPMessage = "Where is AMQPWrapper? " + secretKey

  def receive = {

    case message @ CONFMessage =>
      println("Received: Where is Configurations?")
      println("Reply: 2552")
      self.reply_?("2552")
    case message @ AMQPMessage =>
      println("Received: Where is AMQPWrapper?")
      println("Reply: 2700")
      self.reply_?("2700")
    case message @ _ =>
      self.reply_?("0000")
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
    case allExceptWorkerNumber(messageString, secretKey)=>
      AMQPWrapper.connectToAMQP(messageString, secretKey)
  }
}

object AMQPActor{
    val serviceName = "amqp"
}

class ConsumerActor extends Actor {
  val name = "Consumer: "
  val secretKey = Config.config.getString("project-name.secretKey").get

  def receive = {
      case Delivery(data, secretKey, _, _, _, _) =>
        println("Received from WorkerService: " + new String(data))
    }
}

object ConsumerActor{
    val serviceName = "consumer"
}


