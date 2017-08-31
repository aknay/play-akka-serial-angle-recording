package actors

/**
  * Created by aknay on 20/6/17.
  */

import javax.inject._

import akka.actor._
import akka.serial.{Parity, SerialSettings}
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object ParentActor {
  val arduinoSettings = SerialSettings(115200, 8, false, Parity(0))
}

class ParentActor @Inject()() extends Actor with ActorLogging {

  import ParentActor._

  var arduinoActor: ActorRef = _

  implicit val timeout = Timeout(5.seconds)

  override def preStart: Unit = {
    arduinoActor = context.actorOf(ArduinoActor("/dev/ttyACM1", arduinoSettings), name = "Arduino")
  }

  def receive = {
    case ArduinoActor.Received(angle) =>
      context.actorSelection("/user/*/flowActor").resolveOne().onComplete {
        case Success(websocket) =>
          websocket ! WebSocketActor.sendJson(angle)
        case Failure(e) => throw e
      }
    case _ =>
      log.info("we don't know what to do here in Parent Actor")
  }
}

