package actorproto

import akka.amqp._
import akka.config.Config
import akka.actor.Actor
import collection.mutable.HashMap
import org.apache.commons.codec.net.QCodec
import com.sun.xml.internal.ws.developer.MemberSubmissionAddressing.Validation
import java.lang.String

case class entireMessage(workerNumber: Int, messageString: String)
case class allExceptWorkerNumber(messageString: String)
case class cafData(dataInList: List[HashMap[String, String]])

class WorkDistributorActor extends Actor {
  val name = "WorkDistributor: "

  def receive = {
    case cafData(dataInList) =>
      for(i <- 0 until dataInList.length){
        println(">>" + (dataInList.apply(i)).get("cpu_total").get)
      }
  }
}
object WorkDistributorActor {
    val serviceName = "workDistributor"
}

class WorkerActor extends Actor {
  val name = "Worker: "

  def receive = {
//    case message @ "To Worker: What next?" =>
//      println("Worker Received: What next?")
//      println("Worker Says: Start CAF Communicator")
//      Worker.startCafCommunicator
    case "collectSchema" =>
      modules.startCaf.runSchema
    case "collectInstance" =>
      modules.startCaf.runInstance
  }
}
object WorkerActor {
    val serviceName = "worker"
}

class DirectoryActor extends Actor {
  val name = "Directory: "

  def receive = {

    case message @ "Where is Worker?" =>
      val remoteHost = Config.config.getString("project-name.remoteHost").get
      val remotePort = Config.config.getInt("project-name.remotePort").get
      val replyString: String = remoteHost + "&" + remotePort.toString
      self.reply_?(replyString)

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
      self.reply_?("true")
      println(message)
  }
}
object ConfigurationActor {
    val serviceName = "configuration"
}




