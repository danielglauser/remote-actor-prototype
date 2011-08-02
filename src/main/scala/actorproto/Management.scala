package actorproto

import java.lang.String

object abcd {

  def main(args: Array[String]) {
    runXML
  }

  def runXML = {
    var count = 0
   val response = ""

   val responseXML = scala.xml.XML.loadString(response)

   for( uri <- responseXML \\ "@uri")
    {
      count = count + 1
    }

    println(count)

  }
}
