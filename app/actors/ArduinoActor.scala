package actors

/**
  * Created by aknay on 9/6/17.
  */

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import akka.io.IO
import akka.serial.{Serial, SerialSettings}
import models.Angle

class ArduinoActor(port: String, settings: SerialSettings) extends Actor with ActorLogging {

  import context._

  val messageByteDecoder = context.actorOf(Props[MessageByteDecoder], name = "MessageByteDecoder")
  log.info(s"Requesting manager to open port: ${port}, baud: ${settings.baud}")
  IO(Serial) ! Serial.Open(port, settings)

  override def preStart: Unit = {
    log.info("Arduino actor is in pre start")
  }

  override def postStop() = {

  }

  def receive = {
    case Serial.CommandFailed(cmd, reason) => {
      log.error(s"Connection failed, stopping terminal. Reason: ${reason}")
      context stop self
    }
    case Serial.Opened(port) => {
      log.info(s"Port ${port} is now open.")
      val operator = sender
      context become opened(operator)
      context watch operator
    }
  }

  def opened(operator: ActorRef): Receive = {

    case Serial.Received(data) =>
      messageByteDecoder ! MessageByteDecoder.Decode(data)

    case MessageByteDecoder.Decoded(angle) =>
      parent ! ArduinoActor.Received(angle)

    case Serial.Closed =>
      log.info("Operator closed normally, exiting terminal.")
      context unwatch operator
      context stop self

    case Terminated(`operator`) =>
      log.error("Operator crashed, exiting terminal.")
      context stop self

    case _ =>


  }
}

object ArduinoActor {

  case class Received(angle: Angle)

  //DONT FORGET TO CHANGE classOf[YOUR_ACTOR]
  def apply(port: String, settings: SerialSettings) = Props(classOf[ArduinoActor], port, settings)

}



