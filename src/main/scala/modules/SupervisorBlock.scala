package modules

import akka.actor.Actor
import akka.config.Config
import collection.mutable.HashMap
import scala.Predef._
import scala.collection.JavaConversions._
import java.util.{Map}
import java.net.InetAddress

object Supervisor {
  var gotData = false
  var entireData = List[HashMap[String, String]]()

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

  def getMyIpAddress = {
      var ipAddress = ""
      val en = java.net.NetworkInterface.getNetworkInterfaces

    while(en.hasMoreElements) {
      val element = en.nextElement()
      val interfaceList = element.getInterfaceAddresses
      if(interfaceList.size() == 2){
        if(interfaceList.get(1).getAddress.getHostAddress.contains("10.25"))
          ipAddress = interfaceList.get(1).getAddress.getHostAddress
      }
    }

      ipAddress
  }
  def sendMyIpToDirectory(ipAddress: String) = {
    val directoryHost = Config.config.getString("project-name.directoryHost").get
    val directoryPort = Config.config.getInt("project-name.directoryPort").get

    val destination = Actor.remote.actorFor(DirectoryActor.serviceName, directoryHost, directoryPort)
    destination ! setIpAddressToMap("supervisor", ipAddress)
}

  def getSupervisorIpFromDirectory = {
    val directoryHost = Config.config.getString("project-name.directoryHost").get
    val directoryPort = Config.config.getInt("project-name.directoryPort").get

    val destination = Actor.remote.actorFor(DirectoryActor.serviceName, directoryHost, directoryPort)
    val ipAddress = (destination !! (getIpAddressFromMap("supervisor"))).get

    ipAddress.toString
  }
  def startSupervisor(ipAddress: String) = {

    Actor.remote.start(ipAddress, 5000)
    println("Starting the Supervisor")
    Actor.remote.register(SupervisorActor.serviceName, Actor.actorOf(new SupervisorActor))
  }

  def getWorkerIpFromDirectory = {
    val directoryHost = Config.config.getString("project-name.directoryHost").get
    val directoryPort = Config.config.getInt("project-name.directoryPort").get

    val destination = Actor.remote.actorFor(DirectoryActor.serviceName, directoryHost, directoryPort)
    val ipAddress = (destination !! (getIpAddressFromMap("worker"))).get

    ipAddress.toString
  }
  def connectToWorker(workerIpAddress: String) = {
    Actor.remote.actorFor(WorkerActor.serviceName, workerIpAddress, 5000)
  }

  def run(collectSchema: Boolean, collectInstance: Boolean, factor: Int = 1, strategy: String = "SERIAL", otherStrategy: Int = 1) = {

    val (data, workerCount) = msgDetails(collectSchema, collectInstance, factor, strategy, otherStrategy)
    val myIpAddress = getMyIpAddress
    sendMyIpToDirectory(myIpAddress)

    val supervisorIpAddress = getSupervisorIpFromDirectory
    startSupervisor(supervisorIpAddress)

    val workerIpAddress = getWorkerIpFromDirectory
    val worker = connectToWorker(workerIpAddress)

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
      val process: java.util.Map[String, String] = entireData.apply(i)
      javaDataRepresentation.add(process)
    }

    javaDataRepresentation
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

