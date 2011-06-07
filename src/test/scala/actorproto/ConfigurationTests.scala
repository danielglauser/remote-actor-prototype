import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import akka.actor.Actor._
import akka.actor. {ActorRegistry, Actor, ActorRef}
import actorproto. {ConfigurationActor, ConfigSetting, MessageSetting, PayloadBinding, ActorRepositorySetting}

class ConfigurationSpec extends FlatSpec with ShouldMatchers {

  "A Configuration Actor" should "respond to the following messages" in {
    val PORT = 8675
    Actor.remote.start("localhost", PORT)
    val configurationActor = Actor.remote.actorFor(ConfigurationActor.serviceName, "localhost", PORT)
    var message = MessageSetting("numCollectionMessages", 400)
    configurationActor ! message
    configurationActor ! MessageSetting("numCollectionMessages") should equal (message)
  }

//  it should "throw NoSuchElementException if an empty stack is popped" in {        
//    evaluating { emptyStack.pop() } should produce [NoSuchElementException]
//  }
}