package actors

/**
  * Created by aknay on 20/6/17.
  */

import java.util.Date
import javax.inject._

import akka.actor._
import akka.pattern.pipe
import akka.serial.{Parity, SerialSettings}
import akka.util.Timeout
import dao.AngleDao
import models.{Angle, AngleInfo}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

object ParentActor {
  val arduinoSettings = SerialSettings(115200, 8, false, Parity(0))

  case class GetAnglesForThis(date: Date)

  case class Register(a: ActorRef)

  case class UnRegister(a: ActorRef)

}

class ParentActor @Inject()(angleDao: AngleDao) extends Actor with ActorLogging {

  import ParentActor._

  var arduinoActor: ActorRef = _

  implicit val timeout = Timeout(5.seconds)
  private val webClients = mutable.Set[ActorRef]()

  override def preStart: Unit = {
    arduinoActor = context.actorOf(ArduinoActor("/dev/ttyUSB0", arduinoSettings), name = "Arduino")
  }

  def sendToWebClients(a: Any): Unit = {
    webClients.foreach(ref => ref ! a)
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
      if (webClients.nonEmpty) {
        sendToWebClients(WebSocketActor.sendJson(angle))
      }

    case ParentActor.GetAnglesForThis(date) =>
      val data: Future[Seq[AngleInfo]] = angleDao.getByDate(date)
      pipe(data) to sender

    case Register(ar) =>
      webClients += ar

    case UnRegister(ar) =>
      webClients -= ar

    case _ =>
      log.info("we don't know what to do here in Parent Actor")
  }
}