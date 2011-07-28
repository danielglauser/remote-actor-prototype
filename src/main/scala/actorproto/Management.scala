package actorproto

import akka.actor.Actor._
import akka.actor.Actor
import akka.config._
import akka.amqp.AMQP._
import akka.amqp._
import java.util.Scanner
import java.io.{InputStreamReader, BufferedReader}
import scala.swing._
import event._
import java.lang.{Boolean, String, Runtime}
import collection.mutable.HashMap
import scala.collection.JavaConversions._

object userInterface extends SimpleSwingApplication{
    def top = new MainFrame {
      title = "Work Distributor"

      val workLabel = new Label() {
        text = "Select Work:"
      }

      val collectSchema = new CheckBox("collect Schema")
      val collectInstance = new CheckBox("collect Instance")

      val factorLabel = new Label() {
        text = "Enter factor:"
      }

      val factorField = new TextField() {
        columns = 10
      }

      val strategyLabel = new Label(){
        text = "Select Worker Strategy"
      }

      val strategy = new ButtonGroup
      val serial = new RadioButton("Serial")
      val parallel = new RadioButton("Parallel")
      val other = new RadioButton("Other")
      val resources = new RadioButton("According to Resources")
      val strategyRadios = List(serial, parallel, other, resources)
      strategy.buttons ++= strategyRadios
      strategy.select(serial)

      val otherStrategyLabel = new Label(){
        text = "Enter Number of Workers"
      }

      val otherStrategyField = new TextField() {
        columns = 10
      }

      val submitButton = new Button() {
      text = "Submit"
      }

      contents =  new BoxPanel(Orientation.Vertical){
        contents += workLabel
        contents += collectSchema
        contents += collectInstance

        contents += factorLabel
        contents += factorField

        contents += strategyLabel
        contents ++= strategyRadios

        contents += otherStrategyLabel
        contents += otherStrategyField

        contents += submitButton
      }

      listenTo(submitButton)
      var nClicks = 0
      reactions += {
        case ButtonClicked(b) =>

          var strategySelected = ""
          strategy.selected.get match {
            case `serial` =>  strategySelected = "SERIAL"
            case `parallel` =>  strategySelected = "PARALLEL"
            case `other` => strategySelected = "OTHER"
          }

          nClicks += 1
          submitButton.text = "Sent: " + nClicks

          WorkDistributor.run(collectSchema.selected, collectInstance.selected, factorField.text.toInt, strategySelected, otherStrategyField.text.toInt)

          collectSchema.selected = false
      }
    }
  }

object WorkDistributor {
  var gotData = false
  var entireData: java.util.List[java.util.Map[String, String]] = null

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
    Actor.remote.register(WorkDistributorActor.serviceName, Actor.actorOf(new WorkDistributorActor))
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
    val directoryDest = Actor.remote.actorFor(DirectoryActor.serviceName, "10.25.38.50", 3000)
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

    for(i <- 0 until dataInList.length){
      println(">>" + (dataInList.apply(i)).get("cpu_total").get)
      val process: java.util.Map[String, String] = dataInList.apply(i)
      entireData.add(process)
     }


  }

  def start(request: String) = {
    if(request == "collectSchema") run(true, false)
    if(request == "collectInstance") run(false, true)

    while(!gotData){}
    entireData
  }
}

object Worker {

  def startWorker = {
    println("Starting Worker")
    val remoteHost = Config.config.getString("project-name.remoteHost").get
    val remotePort = Config.config.getInt("project-name.remotePort").get

    Actor.remote.start(remoteHost, remotePort)
    Actor.remote.register(WorkerActor.serviceName, Actor.actorOf(new WorkerActor))
  }

  def main(args: Array[String]) {
    startWorker
  }
}

















//----------------------------------------------------------------------------------------------
//object abcd {
//
//  def main(args: Array[String]) {
//    Runtime.getRuntime.exec("/bin/startRemoteJvm.sh")
//  }
//
//  def runXML = {
//
//    val ps = Runtime.getRuntime.exec("pc.bat")
//    var br = new BufferedReader(new InputStreamReader(ps.getInputStream))
//    var s = ""
//    for (i <- 1 to 5) s = br.readLine()
//    val index = s.indexOf(",")
//
//    println("Cores: " +  Runtime.getRuntime.availableProcessors())
//    println("Free memory available to the JVM: " + Runtime.getRuntime.freeMemory() + " bytes")
//    println("Memory currently in use by the JVM: " + Runtime.getRuntime.totalMemory() + " bytes")
//    println("CPU Usage: " + s.substring(index+2, s.length()-1) + " percent")
//
//  }
//
//
//
//
//  def main(args: Array[String]) {
//    runXML
//  }
//}
//
//object Directory {
//
//  def run = {
//    val directoryPort = Config.config.getInt("project-name.directoryPort").get
//    println("Starting the directory on " + directoryPort)
//    Actor.remote.register(DirectoryActor.serviceName, Actor.actorOf(new DirectoryActor))
//  }
//
//  def main(args: Array[String]) {
//    run
//  }
//}
//
//object Configurations {
//
//  def run = {
//    println("Starting the Configurations on 2552")
//    Actor.remote.start("localhost", 2552).register(ConfigurationActor.serviceName, Actor.actorOf(new ConfigurationActor))
//  }
//
//  def main(args: Array[String]) {
//    run
//  }
//}
//
//object AMQPWrapper {
//
//  def getKey = {
//      println("Enter Key to Authenticate")
//      val secretKey = new Scanner(System.in).next()
////      val secretKey = "secret"
//
//      secretKey
//  }
//
//  def start = {
//    println("Starting the Configurations on 2700")
//    Actor.remote.start("localhost", 2700).register(AMQPActor.serviceName, Actor.actorOf(new AMQPActor))
//  }
//
//  def sendToDirectory(secretKey: String) = {
//    val directoryPort = Config.config.getInt("project-name.directoryPort").get
//    val directoryDest = Actor.remote.actorFor(DirectoryActor.serviceName, "localhost", directoryPort)
//
//    val configurationPort = (directoryDest !! "Where is Configurations? "+secretKey).get
//
//    configurationPort.toString.toInt
//  }
//
//  def connectToConfigurations(configurationPort: Int) = {
//    val configurationDest = Actor.remote.actorFor(ConfigurationActor.serviceName, "localhost", configurationPort)
//    configurationDest ! "AMQPWrapper -> Configuration"
//  }
//
//  def connectToAMQP(messageString: String, secretKey: String) = {
//
////  **** Pattern 0 ****
//    val connection = AMQP.newConnection()
//    val exchangeParameters = ExchangeParameters("request")
//    val producer = AMQP.newProducer(connection, ProducerParameters(Some(exchangeParameters), producerId = Some("my_producer")))
//
//    producer ! Message(messageString.getBytes, secretKey)
//  }
//
//  def runReplyConsumer = {
//    val secretKey = Config.config.getString("project-name.secretKey").get
//
////  **** Pattern 0 ****
//    val connection = AMQP.newConnection()
//    val exchangeParameters = ExchangeParameters("response")
//
//    val myConsumer = AMQP.newConsumer(connection, ConsumerParameters(secretKey, actorOf(new ConsumerActor), None, Some(exchangeParameters)))
//  }
//
//  def run {
//    start
//    val secretKey = getKey
//    val configurationPort = sendToDirectory(secretKey)
//    connectToConfigurations(configurationPort)
//  }
//
//  def main(args: Array[String]) {
//    run
//  }
//}
//
//object Consumer {
//
////  def startCafCommunicator = {
////    Actor.remote.start("10.25.38.50", 3000).register(CafCommunicatorActor.serviceName, Actor.actorOf(new CafCommunicatorActor))
////    println("Started LocalActor on port 3000" )
////  }
//
//  def runConsumer = {
//
//    val secretKey = Config.config.getString("project-name.secretKey").get
//
////  **** Pattern 0 ****
//    val connection = AMQP.newConnection()
//    val exchangeParameters = ExchangeParameters("request")
//
//    val myConsumer = AMQP.newConsumer(connection, ConsumerParameters(secretKey, actorOf(new ConsumerActor), None, Some(exchangeParameters)))
//  }
//
//  def runReply(dataInList: List[HashMap[String, String]]) = {
//
////  **** Pattern 0 ****
//    val connection = AMQP.newConnection()
//    val exchangeParameters = ExchangeParameters("response")
//    val producer = AMQP.newProducer(connection, ProducerParameters(Some(exchangeParameters), producerId = Some("my_producer")))
//
//    val messageString = "CHANGE INPUT"
//    val secretKey = "secret"
//
//    producer ! Message(messageString.getBytes, secretKey)
//  }
//
//  def main(args: Array[String]) {
////    startCafCommunicator
////    runConsumer
//  }
//}