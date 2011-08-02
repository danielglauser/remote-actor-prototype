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
import akka.config.Config

case class cafData(dataInList: List[HashMap[String, String]])

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
		      val fullyQualifiedClass = new FullyQualifiedClass("HypericSigar", "Arp", "0.0.1.0")
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

    val uriMap = getURI(response)

    for (i <- 0 until uriMap.size)
      println("uriMap: " + uriMap.get(i).get)

    val dataInList = getListFromUri(uriMap.get(0).get)

    connectToLocal(dataInList)
  }

  def getNumEventReceived = {
    numEventReceived
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
    println("uriCount: " + uriCount)
    uriMap
  }

  def getListFromUri(uri: String) = {
    DataParser.run(uri)
  }

  def connectToLocal(dataInList: List[HashMap[String, String]]) = {
    val localHost = Config.config.getString("project-name.localHost").get
    val localPort = Config.config.getInt("project-name.localPort").get
    println("Starting Remote.." )
    val destination = Actor.remote.actorFor(SupervisorActor.serviceName, localHost, localPort)
    destination ! cafData(dataInList)
  }
}

object DataParser {
  def run(uri: String) = {
    println("Loading file from: " + uri)
    val procInstance = scala.xml.XML.loadFile(uri)

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
}

