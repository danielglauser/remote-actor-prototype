package actorproto

import akka.actor. {Actor, ActorRef}
import akka.amqp._


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

class ConsumerActor extends Actor {
  val name = "Consumer: "

  def receive = {
      case Delivery(data, "secret", _, _, _, _) =>
        println("Received from Worker: " + new String(data))
    }
}

object ConsumerActor{
    val serviceName = "consumer"
}


