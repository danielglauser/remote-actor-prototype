package modules

import java.util.regex.Pattern
import java.util.UUID
import com.vmware.commonagent.contracts.dependencies.FullyQualifiedClass
import com.vmware.commonagent.contracts.exceptions.CafException
import com.vmware.commonagent.eventing.Eventing
import com.vmware.commonagent.subsys.clients.{SinglePmeCafClient}
import com.vmware.commonagent.mocks.ManagementAgentCommunicationMock
import com.vmware.commonagent.contracts._
import com.vmware.commonagent.common.core.CafClientEvent
import com.vmware.commonagent.subsys.communication.ManagementAgentCommunication

object startCaf {

  def collectSchema1(clientId: UUID, requestId: UUID, smid: UUID, client: SinglePmeCafClient, subscriber: MyCafSubscriber2) = {
  try {
	  client.collectSchema(clientId, requestId, smid)
    waitForEvent(subscriber)
  } catch  {
      case ex: Exception =>
		    println("ERROR in CollectScehema")
	      throw new CafException(ex)
  }
 }

  def collectInstances1(clientId: UUID, requestId: UUID, smid: UUID, client: SinglePmeCafClient, subscriber: MyCafSubscriber2) = {
		  try {
		      val fullyQualifiedClass = new FullyQualifiedClass("vCM", "Process", "1.0.0.0")
		      client.collectInstances(clientId, requestId, smid,fullyQualifiedClass)
          waitForEvent(subscriber)

		   } catch {
          case ex: Exception =>
			      System.out.println("ERROR in CollectInstance");
		        throw new CafException(ex);
		   }
  }

  def waitForEvent(subscriber: MyCafSubscriber2) = {
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
    val subscriber: MyCafSubscriber2 = new MyCafSubscriber2()

    val eventing: IEventing =  new Eventing
    val communication = new ManagementAgentCommunication(eventing)

    eventing.subscribe(subscriber, "CafClientEvent", Pattern.compile("\\w"))
    (subscriber, eventing, communication)
  }

  def main(args: Array[String]) {
  val requestId = UUID.fromString("DEADBEEF-0000-0000-0000-BAADCAFDA1A6");
	val clientId = UUID.fromString("DEADBEEF-0000-0000-0000-BAADF00D0001");
	val smid = UUID.fromString("DEADBEEF-0000-0000-0000-BAADF00D0001");

  val (subscriber, eventing, communication) = subscribeForEvent
  val client =  new SinglePmeCafClient(eventing, communication)

  collectSchema1(requestId, clientId, smid, client, subscriber)
//  collectInstances1(requestId, clientId, smid, client, subscriber)
  }
}

class MyCafSubscriber extends ISubscriber{

  var numEventReceived = 0

  def eventNotify(event: IEvent) = {
    println("Inside eventNotify of MyCafSubscriber")

    if (event.isInstanceOf[CafClientEvent])
      numEventReceived+1

    val cafClientEvent = event.asInstanceOf[CafClientEvent]

    val contentId = cafClientEvent.getContentId()
    println("Content ID: " + contentId)

    val description = cafClientEvent.getDescription()
    println("Description: " + description)

    val eventSource = cafClientEvent.getEventSource()
    println("Event Source: " + eventSource)

    val eventName = cafClientEvent.getEventName()
    println("Event Name: " + eventName)

    val clientId = cafClientEvent.getContext().getClientId().toString()
    println("Client ID" + clientId)

    val requestId = cafClientEvent.getContext().getRequestId().toString()
    println("Request ID" + requestId)

    val smId = cafClientEvent.getContext().getSmid().toString()
    println("Sm I: " +smId)

    val response = cafClientEvent.getResponseMem()
    println("Response Data: " + response)
  }

  def getNumEventReceived = {
    numEventReceived
  }
}

