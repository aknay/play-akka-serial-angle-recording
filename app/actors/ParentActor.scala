package actors

/**
  * Created by aknay on 20/6/17.
  */

import java.util.Date
import javax.inject._

import akka.actor._
import akka.serial.{Parity, SerialSettings}
import akka.util.Timeout
import dao.AngleDao
import models.Angle

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object ParentActor {
  val arduinoSettings = SerialSettings(115200, 8, false, Parity(0))

  case class GetAnglesForThis(date: Date)

}

class ParentActor @Inject()(angleDao: AngleDao) extends Actor with ActorLogging {

  import ParentActor._

  var arduinoActor: ActorRef = _

  implicit val timeout = Timeout(5.seconds)

  override def preStart: Unit = {
    arduinoActor = context.actorOf(ArduinoActor("/dev/ttyUSB0", arduinoSettings), name = "Arduino")
  }

  def saveAngle(angle: Angle) = {
    val isSameAsLastEntry: Future[Boolean] = angleDao.getLatestEntry
      .map(a => if (a.x == angle.x && a.y == angle.y) true else false)

    isSameAsLastEntry.map {
      case true =>
      case false => angleDao.insert(angle).map(_ => ())
    }
  }

  def receive = {
    case ArduinoActor.Received(angle) =>
      saveAngle(angle)

      context.actorSelection("/user/*/flowActor").resolveOne().onComplete {
        case Success(websocket) =>
          websocket ! WebSocketActor.sendJson(angle)
        case Failure(e) => throw e
      }

    case ParentActor.GetAnglesForThis(date) =>
      sender ! angleDao.getByDate(date)

    case _ =>
      log.info("we don't know what to do here in Parent Actor")
  }
}