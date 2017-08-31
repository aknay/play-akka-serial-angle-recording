package actors

import akka.actor.{Actor, ActorLogging}
import akka.util.ByteString
import models.Angle

/**
  * Created by aknay on 5/30/17.
  */

class MessageByteDecoder extends Actor with ActorLogging {
  var receivedData: ByteString = ByteString("")
  val minBufferSize = 60
  val endFrame: Byte = ByteString('D').head

  import context._

  override def receive: Receive = {
    case MessageByteDecoder.Decode(in) => {
      receivedData = receivedData.concat(in)

      if (receivedData.length > minBufferSize) {
        val str = receivedData.takeWhile(_.compareTo(endFrame) != 0)
        receivedData = receivedData.drop(str.length + 1) //we need to remove 'D' also
        val angle: Array[String] = str.utf8String.split(",")

        if (angle.length == 3) { //number of angle is 3
          val firstTwoMakerLength = 2
          val x = angle.head.drop(firstTwoMakerLength)
          val y = angle {1}.drop(firstTwoMakerLength)
          val z = angle {2}.drop(firstTwoMakerLength)

          val angleModel = Angle(x.toDouble.toInt, y.toDouble.toInt, z.toDouble.toInt)
          parent ! MessageByteDecoder.Decoded(angleModel)
        }
      }
    }
  }
}

object MessageByteDecoder {

  case class Decode(byteString: ByteString)

  case class Decoded(angle: Angle)

}