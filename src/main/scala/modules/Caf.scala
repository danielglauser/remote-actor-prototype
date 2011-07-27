package modules

import java.util.regex.Pattern
import com.vmware.commonagent.contracts.dependencies.FullyQualifiedClass
import com.vmware.commonagent.contracts.exceptions.CafException
import com.vmware.commonagent.eventing.Eventing
import com.vmware.commonagent.subsys.clients.{SinglePmeCafClient}
import com.vmware.commonagent.contracts._
import com.vmware.commonagent.common.core.CafClientEvent
import com.vmware.commonagent.subsys.communication.ManagementAgentCommunication
import java.io._
import java.lang.{StringBuffer}
import java.util.{Random, UUID}
import collection.mutable.HashMap
import akka.actor.Actor
import actorproto.CafCommunicationActor

object startCaf {

  def collectSchema1(clientId: UUID, smid: UUID, client: SinglePmeCafClient, subscriber: MyCafSubscriber) = {
    val rand = new Random()
    val requestIdString: String = "DEADBEEF-0000-0000-0000-DEADBEEF00" + rand.nextInt(100)
    val requestId = UUID.fromString(requestIdString)
  try {
	  client.collectSchema(clientId, requestId, smid)
    waitForEvent(subscriber)
  } catch  {
      case ex: Exception =>
		    println("ERROR in CollectScehema")
	      throw new CafException(ex)
  }
 }

  def collectInstances1(clientId: UUID, smid: UUID, client: SinglePmeCafClient, subscriber: MyCafSubscriber) = {
      val rand = new Random()
      val requestIdString: String = "DEADBEEF-1111-0000-0000-DEADBEEF00" + rand.nextInt(100)
      val requestId = UUID.fromString(requestIdString)
		  try {
		      val fullyQualifiedClass = new FullyQualifiedClass("HypericSigar", "Proc", "0.0.1.0")
		      client.collectInstances(clientId, requestId, smid,fullyQualifiedClass)
          waitForEvent(subscriber)

		   } catch {
          case ex: Exception =>
			      System.out.println("ERROR in CollectInstance");
		        throw new CafException(ex);
		   }
  }

  def waitForEvent(subscriber: MyCafSubscriber) = {
      val intervalSleepMs = 500;
      val totalIntervals = 5;

      var isEventReceived = false;
      for (ii <- 0 to intervalSleepMs by totalIntervals ) {
        if(!isEventReceived){
          Thread.sleep(intervalSleepMs);
          isEventReceived = subscriber.getNumEventReceived > 0
        }
      }

      isEventReceived;
  }

  def subscribeForEvent = {
    val subscriber: MyCafSubscriber = new MyCafSubscriber()

    val eventing: IEventing =  new Eventing
    val communication = new ManagementAgentCommunication(eventing)

    eventing.subscribe(subscriber, "CafClientEvent", Pattern.compile("\\w"))
    (subscriber, eventing, communication)
  }

  def runInstance = {
    val clientId = UUID.fromString("DEADBEEF-0000-0000-0000-BAADF00D0001");
    val smid = UUID.fromString("DEADBEEF-0000-0000-0000-BAADF00D0001");

    val (subscriber, eventing, communication) = subscribeForEvent
    val client =  new SinglePmeCafClient(eventing, communication)

    collectInstances1(clientId, smid, client, subscriber)
  }

  def runSchema = {
    val clientId = UUID.fromString("DEADBEEF-0000-0000-0000-BAADF00D0001");
    val smid = UUID.fromString("DEADBEEF-0000-0000-0000-BAADF00D0001");

    val (subscriber, eventing, communication) = subscribeForEvent
    val client =  new SinglePmeCafClient(eventing, communication)

    collectSchema1(clientId, smid, client, subscriber)
  }

  def main(args: Array[String]) {
    runInstance
  }
}

class MyCafSubscriber extends ISubscriber{

  var numEventReceived = 0

  def eventNotify(event: IEvent) = {

    if (event.isInstanceOf[CafClientEvent])
      numEventReceived+1

    val cafClientEvent = event.asInstanceOf[CafClientEvent]

    val response = cafClientEvent.getResponseMem()
    println("Response Data: " + response)

    val mCount = getManifestInFile(response)
    val manifestList = getManifestInList(mCount)
    val UrlList = getUrlInList(manifestList)

    for(i <- 0 until UrlList.length){
      val dataInString = getDataInString(UrlList.apply(i))
      getDataInFile(dataInString)
    }

    val dataInList = getDataInList

    connectToLocal(dataInList)
  }

  def getNumEventReceived = {
    numEventReceived
  }

  def getManifestInFile(response:String) = {
    var mCount = 1
    var fileName = "ManifestCollection" + mCount + ".xml"
    var newFile = new File(fileName)

    while(newFile.exists()){
      mCount = mCount+1
      fileName = "ManifestCollection" + mCount + ".xml"
      newFile = new File(fileName)
    }

    val fileWriter = new FileWriter(newFile)
    val bufferedWriter =  new BufferedWriter(fileWriter)
    bufferedWriter.write(response)
    bufferedWriter.close()

    mCount
  }

  def getManifestInList(mCount: Int) = {
    var urlCount = 0
    var tempList = List[String]()
    var manifestList = List[String]()
    val fileName = "ManifestCollection" + mCount +".xml"

    val manifest =  scala.xml.XML.loadFile(fileName)
    for(uri <- manifest \\ "@uri"){
      tempList = tempList.::(uri.text)
      urlCount = urlCount + 1
    }
    if(urlCount == 2){
      manifestList = manifestList.::(tempList.apply(1))
    }
    if(urlCount == 4){
      manifestList = manifestList.::(tempList.apply(1))
      manifestList = manifestList.::(tempList.apply(3))
    }

    manifestList.reverse
  }

  def getUrlInList(manifestList: List[String]) = {
    var UrlList = List[String]()

    for( i <- 0 until manifestList.length){
      UrlList = UrlList.::(manifestList.apply(i).substring(7))
    }
    UrlList.reverse
  }

  def getDataInString(fileUrlString: String) = {
    val data = new StringBuffer(1000)

    val reader = new BufferedReader(new FileReader(fileUrlString))
    var buf = new Array[Char](1024)
    var numRead = 0

    while(reader.read(buf) != -1){
      val readData = String.valueOf(buf)

      data.append(readData)
      buf = new Array[Char](1024)
    }

    reader.close()
    val dataInString = data.toString

    dataInString
  }

  def getDataInFile(dataInString: String) = {
    var count = 1
    var fileName = "file" + count + ".xml"
    var newFile = new File(fileName)

    while(newFile.exists()){
      count = count+1
      fileName = "file" + count + ".xml"
      newFile = new File(fileName)
    }

    val fileWriter = new FileWriter(newFile)
    val bufferedWriter = new BufferedWriter(fileWriter)
    bufferedWriter.write(dataInString)
    bufferedWriter.close()
  }

  def getDataInList = {
    DataParser.run
  }

  def connectToLocal(dataInList: List[HashMap[String, String]]) = {
    println("Starting Remote.." )
    val destination = Actor.remote.actorFor(CafCommunicationActor.serviceName, "10.25.38.50", 3000)
    destination ! actorproto.cafData(dataInList)
  }
}

object DataParser {
  def run = {
    val procInstance = scala.xml.XML.loadFile("instance2.xml")

    var totalList = List[HashMap[String, String]]()
    var procMap = new HashMap[String, String]

    for(process <- procInstance \\ "Proc") {
      for(mem_minor <- process \\ "mem_minor_faults"){
        procMap += "mem_minor" -> mem_minor.text
      }
      for(cpu_total <- process \\ "cpu_total"){
        procMap += "cpu_total" -> cpu_total.text
      }
      for(state_name <- process \\ "state_name"){
        procMap += "state_name" -> state_name.text
      }
      for(credName_group <- process \\ "credName_group"){
        procMap += "credName_group" -> credName_group.text
      }
      for(pid <- process \\ "pid"){
        procMap += "pid" -> pid.text
      }
      for(mem_page_faults <- process \\ "mem_page_faults"){
        procMap += "mem_page_faults" -> mem_page_faults.text
      }
      for(mem_resident <- process \\ "mem_resident"){
        procMap += "mem_resident" -> mem_resident.text
      }
      for(cred_gid <- process \\ "cred_gid"){
        procMap += "cred_gid" -> cred_gid.text
      }
      for(cpu_percent <- process \\ "cpu_percent"){
        procMap += "cpu_percent" -> cpu_percent.text
      }
      for(mem_size <- process \\ "mem_size"){
        procMap += "mem_size" -> mem_size.text
      }
      for(state_ppid <- process \\ "state_ppid"){
        procMap += "state_ppid" -> state_ppid.text
      }
      for(state_tty <- process \\ "state_tty"){
        procMap += "state_tty" -> state_tty.text
      }
      for(cpu_user <- process \\ "cpu_user"){
        procMap += "cpu_user" -> cpu_user.text
      }
      for(cred_euid <- process \\ "cred_euid"){
        procMap += "cred_euid" -> cred_euid.text
      }
      for(state_threads <- process \\ "state_threads"){
        procMap += "state_threads" -> state_threads.text
      }
      for(state_priority <- process \\ "state_priority"){
        procMap += "state_priority" -> state_priority.text
      }
      for(mem_share <- process \\ "mem_share"){
        procMap += "mem_share" -> mem_share.text
      }
      for(credName_user <- process \\ "credName_user"){
        procMap += "credName_user" -> credName_user.text
      }
      for(mem_share <- process \\ "mem_share"){
        procMap += "mem_share" -> mem_share.text
      }
      for(state_state <- process \\ "state_state"){
        procMap += "state_state" -> state_state.text
      }
      for(state_nice <- process \\ "state_nice"){
        procMap += "state_nice" -> state_nice.text
      }
      for(state_processor <- process \\ "state_processor"){
        procMap += "state_processor" -> state_processor.text
      }
      for(cpu_sys <- process \\ "cpu_sys"){
        procMap += "cpu_sys" -> cpu_sys.text
      }
      for(time_sys <- process \\ "time_sys"){
        procMap += "time_sys" -> time_sys.text
      }
      for(mem_major_faults <- process \\ "mem_major_faults"){
        procMap += "mem_major_faults" -> mem_major_faults.text
      }
      for(cred_egid <- process \\ "cred_egid"){
        procMap += "cred_egid" -> cred_egid.text
      }
      for(time_user <- process \\ "time_user"){
        procMap += "time_user" -> time_user.text
      }
      for(time_total <- process \\ "time_total"){
        procMap += "time_total" -> time_total.text
      }

      totalList = totalList.::(procMap)
      procMap = new HashMap[String, String]
    }

    for(i <- 0 until totalList.length){
      println(">>" + (totalList.apply(i)).get("cpu_total").get)
    }

    totalList
  }

  def main(args: Array[String]) {
    run
  }
}

