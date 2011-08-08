package modules

import akka.actor.Actor
import akka.config.Config
import collection.mutable.HashMap
import scala.Predef._
import scala.collection.JavaConversions._
import java.util.{Map}
import measurements.Profiling

object Supervisor {
  var gotData = false
  var entireData = List[HashMap[String, String]]()

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

  def sendTimeToDirectory(timeFor: String, time: String) = {
    val directoryHost = Config.config.getString("project-name.directoryHost").get
    val directoryPort = Config.config.getInt("project-name.directoryPort").get

    val destination = Actor.remote.actorFor(DirectoryActor.serviceName, directoryHost, directoryPort)
    destination ! perfStats(timeFor, time)
  }

  def run(request: String) = {
    val start = System.nanoTime()
    val myIpAddress = getMyIpAddress
    sendMyIpToDirectory(myIpAddress)

    val supervisorIpAddress = getSupervisorIpFromDirectory
    startSupervisor(supervisorIpAddress)

    val workerIpAddress = getWorkerIpFromDirectory
    val worker = connectToWorker(workerIpAddress)

    worker ! request
    println("Messages Sent to Worker")

    val end = System.nanoTime() - start
    val supervisorBootstrapTime = Profiling.formatTime(end)
    sendTimeToDirectory("supervisorBootstrapTime", supervisorBootstrapTime)
  }

  def setData(dataInList: List[HashMap[String, String]]) = {
    entireData = dataInList
  }

  def start(request: String) = {
    Supervisor.run(request)
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

