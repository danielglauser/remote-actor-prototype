import actorproto._
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import akka.actor.Actor._
import akka.actor. {ActorRegistry, Actor, ActorRef}
import sun.misc.resources.Messages

class ClientSpec extends FlatSpec with ShouldMatchers {

  "A DataCollectionActor" should "return Ack after the sending the message" in {
    val PORT = 2552
    Actor.remote.start("localhost", PORT)
    val dataCollectionActor = Actor.remote.actorFor("actorproto.DataCollectionActor", "localhost", 2552)
    Actor.remote.register(Proxy.serviceName, Actor.actorOf( new Proxy(dataCollectionActor, remediationActor, configurationActor) ))

    var message = (Client.tabulateManyMessages(1, List.apply[String]("collect"))).apply(0)
    (dataCollectionActor !! message).get should equal ("ACK")
  }
}