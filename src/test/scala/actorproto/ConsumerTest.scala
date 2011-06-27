package actorproto

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class ConsumerSpec extends FlatSpec with ShouldMatchers {

  "Directory" should "Start" in {
    Directory.run

  }

  "Configuration" should "Start" in {
    Configurations.run
  }

  "Consumer" should "Start" in {
    Consumer.run
  }

  "AMQPWrapper" should "Start" in {
    AMQPWrapper.run
  }

  "Worker" should "Start" in {
    Worker.run
  }



}