package modules

import java.util.regex.Pattern
import com.vmware.commonagent.contracts.dependencies.FullyQualifiedClass
import com.vmware.commonagent.contracts.exceptions.CafException
import com.vmware.commonagent.eventing.Eventing
import com.vmware.commonagent.subsys.clients.{SinglePmeCafClient}
import com.vmware.commonagent.contracts._
import com.vmware.commonagent.common.core.CafClientEvent
import com.vmware.commonagent.subsys.communication.ManagementAgentCommunication
import java.util.{Random, UUID}
import collection.mutable.HashMap
import akka.actor.Actor
import akka.config.Config

case class manifest(response: String)
case class cafData(dataInList: List[HashMap[String, String]])

object InitializeCaf {

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

    connectToWorker(response)
  }

  def connectToWorker(response: String) = {
    val remoteHost = Config.config.getString("project-name.remoteHost").get
    val remotePort = Config.config.getInt("project-name.remotePort").get
    println("Connecting to Worker.." )
    val destination = Actor.remote.actorFor(WorkerActor.serviceName, remoteHost, remotePort)
    destination ! manifest(response)
  }

  def getNumEventReceived = {
    numEventReceived
  }
}

class CollectInstanceActor extends Actor {
  val name = "CollectInstance: "

  def receive = {
    case _ =>
      InitializeCaf.runInstance
  }
}
object CollectInstanceActor {
  val serviceName = "collectInstance"
}


