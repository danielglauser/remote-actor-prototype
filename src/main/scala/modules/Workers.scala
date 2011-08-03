package modules

import akka.actor.Actor
import akka.config.Config
import collection.mutable.HashMap

object Worker {

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
    destination ! setIpAddressToMap("worker", ipAddress)
}

  def getWorkerIpFromDirectory = {
    val directoryHost = Config.config.getString("project-name.directoryHost").get
    val directoryPort = Config.config.getInt("project-name.directoryPort").get

    val destination = Actor.remote.actorFor(DirectoryActor.serviceName, directoryHost, directoryPort)
    val ipAddress = (destination !! (getIpAddressFromMap("worker"))).get

    ipAddress.toString
}
  def startWorker(workerIpAddress: String) = {
    println("Starting Worker")
    Actor.remote.start(workerIpAddress, 5000)
    Actor.remote.register(WorkerActor.serviceName, Actor.actorOf(new WorkerActor))
  }
  def startCollectInstanceActor = {
    Actor.remote.register(CollectInstanceActor.serviceName, Actor.actorOf(new CollectInstanceActor))
  }
  def startCollectSchemaActor = {
    Actor.remote.register(CollectSchemaActor.serviceName, Actor.actorOf(new CollectSchemaActor))
  }

  def processResponse(response: String) = {
    val uriMap = getURI(response)

    val dataInList = getListFromUri(uriMap.get(0).get)

    connectToSupervisor(dataInList)
  }
  def getURI(response: String) = {
    var uriCount = 0
    var uriMap = new HashMap[Int, String]()
    val manifest = scala.xml.XML.loadString(response)

    for(uri <- manifest \\ "@uri"){
      val temp = uri.text.substring(7)
      if(temp.contains(".provider-data")){
        uriMap += uriCount -> temp
        uriCount = uriCount + 1
      }
    }

    uriMap
  }
  def getListFromUri(uri: String) = {
//    DataParser.processData(uri)
      DataParser.fileSystemData(uri)
  }
  def connectToSupervisor(dataInList: List[HashMap[String, String]]) = {
    val supervisorIpAddress = getSupervisorIpFromDirectory
    println("Connecting to Supervisor.." )
    val destination = Actor.remote.actorFor(SupervisorActor.serviceName, supervisorIpAddress, 5000)
    destination ! cafData(dataInList)
  }
  def getSupervisorIpFromDirectory = {
    val directoryHost = Config.config.getString("project-name.directoryHost").get
    val directoryPort = Config.config.getInt("project-name.directoryPort").get

    val destination = Actor.remote.actorFor(DirectoryActor.serviceName, directoryHost, directoryPort)
    val ipAddress = (destination !! (getIpAddressFromMap("supervisor"))).get

    ipAddress.toString
  }

  def sendToCollectInstanceActor(request: String) = {
    val workerIpAddress = getWorkerIpFromDirectory
    println("Connecting to collectInstanceActor.." )
    val destination = Actor.remote.actorFor(CollectInstanceActor.serviceName, workerIpAddress, 5000)
    destination ! request
  }

  def sendToCollectSchemaActor(request: String) = {
    val workerIpAddress = getWorkerIpFromDirectory
    println("Connecting to collectSchemaActor.." )
    val destination = Actor.remote.actorFor(CollectSchemaActor.serviceName, workerIpAddress, 5000)
    destination ! request
  }

  def main(args: Array[String]) {
    val myIpAddress = getMyIpAddress
    sendMyIpToDirectory(myIpAddress)

    val workerIpAddress = getWorkerIpFromDirectory
    startWorker(workerIpAddress)
    startCollectSchemaActor
    startCollectInstanceActor
  }
}

class WorkerActor extends Actor {
  val name = "Worker: "

  def receive = {
    case message @ "collectSchema" =>
      Worker.sendToCollectSchemaActor(message.toString)
    case message @ "collectInstance" =>
      Worker.sendToCollectInstanceActor(message.toString)
    case manifest(response) =>
      Worker.processResponse(response)

  }
}
object WorkerActor {
    val serviceName = "worker"
}
