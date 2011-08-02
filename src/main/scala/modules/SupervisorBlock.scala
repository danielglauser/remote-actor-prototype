package modules

import akka.actor.Actor
import akka.config.Config
import collection.mutable.HashMap
import scala.Predef._
import scala.collection.JavaConversions._
import java.util.{Map}

object Supervisor {
  var gotData = false
  var entireData = List[HashMap[String, String]]()

  def initializeAllActors = {
    val localHost = Config.config.getString("project-name.localHost").get
    val localPort = Config.config.getInt("project-name.localPort").get

    Actor.remote.start(localHost, localPort)
    startWorkerDistributor
    startDirectory
    startConfigurations
  }

  def startWorkerDistributor = {
    println("Starting the workerDistributor")
    Actor.remote.register(SupervisorActor.serviceName, Actor.actorOf(new SupervisorActor))
  }

  def startDirectory = {
    println("Starting the directory")
    Actor.remote.register(DirectoryActor.serviceName, Actor.actorOf(new DirectoryActor))
  }

  def startConfigurations = {
    println("Starting the Configurations")
    Actor.remote.register(ConfigurationActor.serviceName, Actor.actorOf(new ConfigurationActor))
  }

  def whereIsWorker = {
    val localHost = Config.config.getString("project-name.localHost").get
    val localPort = Config.config.getInt("project-name.localPort").get

    val directoryDest = Actor.remote.actorFor(DirectoryActor.serviceName, localHost, localPort)
    val reply = (directoryDest !! "Where is Worker?").get

    val replyString = reply.toString
    val index = replyString.indexOf('&')
    val workerHost = replyString.substring(0, index)
    val workerPort = replyString.substring(index+1).toInt

   (workerHost, workerPort)
  }

  def connectToWorker(workerHost: String, workerPort: Int) = {
    Actor.remote.actorFor(WorkerActor.serviceName, workerHost, workerPort)
  }

  def msgDetails(collectSchema: Boolean, collectInstance: Boolean, factor: Int, strategy: String, otherStrategy: Int) = {

    var rawMessageList = List[String]()

    if(collectSchema)  rawMessageList = rawMessageList ::: List("collectSchema")
    if(collectInstance) rawMessageList = rawMessageList ::: List("collectInstance")

    var finalMessageList = List[String]()
    for (i <- 0 until rawMessageList.length by factor){
      val tempData = rawMessageList.apply(i)
      finalMessageList = finalMessageList ::: List(tempData)
    }

    var workerCount = 0
    if(strategy == "SERIAL") workerCount = 1
    if(strategy == "PARALLEL") workerCount = 5
    if(strategy == "OTHER") workerCount = otherStrategy

    (finalMessageList, workerCount)

  }

  def run(collectSchema: Boolean, collectInstance: Boolean, factor: Int = 1, strategy: String = "SERIAL", otherStrategy: Int = 1) = {

    val (data, workerCount) = msgDetails(collectSchema, collectInstance, factor, strategy, otherStrategy)
    println(data + " " + workerCount)
    initializeAllActors
    val(workerHost, workerPort) = whereIsWorker
    val worker = connectToWorker(workerHost, workerPort)

    val messages: List[String] = data
    messages.foreach { worker ! _ }
    println("Messages Sent to Worker")
  }

  def setData(dataInList: List[HashMap[String, String]]) = {
    entireData = dataInList
  }

  def start(request: String) = {
    if(request == "collectSchema") run(true, false)
    if(request == "collectInstance") run(false, true)

    while(!gotData){}

    val javaDataRepresentation: java.util.List[java.util.Map[String, String]] = new java.util.ArrayList[Map[String, String]]()

    for(i <- 0 until entireData.length){
//      println(">>" + (entireData.apply(i)).get("cpu_total").get)
      val process: java.util.Map[String, String] = entireData.apply(i)
      javaDataRepresentation.add(process)
    }

    javaDataRepresentation
  }

  def startRps(request: String) = {
    if(request == "collectSchema") run(true, false)
    if(request == "collectInstance") run(false, true)

    while(!gotData){ }
    entireData.reverse
  }
}

class SupervisorActor extends Actor {
  val name = "Supervisor: "

  def receive = {
    case cafData(dataInList) =>
      println("Received Data")
      Supervisor.setData(dataInList)
      Supervisor.gotData = true
  }
}
object SupervisorActor {
    val serviceName = "Supervisor"
}

