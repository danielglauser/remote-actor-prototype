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
import java.io._
import java.lang.{StringBuffer}

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

  def main(args: Array[String]) {

	val clientId = UUID.fromString("DEADBEEF-0000-0000-0000-BAADF00D0001");
	val smid = UUID.fromString("DEADBEEF-0000-0000-0000-BAADF00D0001");

  val (subscriber, eventing, communication) = subscribeForEvent
  val client =  new SinglePmeCafClient(eventing, communication)

//  collectSchema1(clientId, smid, client, subscriber)
  collectInstances1(clientId, smid, client, subscriber)
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
    val UrlList = getUrlFromList(manifestList)

    for(i <- 0 until UrlList.length){
      val dataInString = getDataInString(UrlList.apply(i))
      dataInFile(dataInString)
    }
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
    var manifestList = List[String]()
    val fileName = "ManifestCollection" + mCount +".xml"

    val manifest =  scala.xml.XML.loadFile(fileName)
    for(uri <- manifest \\ "@uri"){
      manifestList = manifestList.::(uri.text)
    }
    manifestList
  }

  def getUrlFromList(manifestList: List[String]) = {
    var UrlList = List[String]()

    for( i <- 0 until manifestList.length){
      UrlList = UrlList.::(manifestList.apply(i).substring(7))
    }
    UrlList
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

  def dataInFile(dataInString: String) = {
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

  def getNumEventReceived = {
    numEventReceived
  }
}

