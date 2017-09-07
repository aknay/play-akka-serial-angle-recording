package actors

/**
  * Created by aknay on 8/6/17.
  */

import java.text.SimpleDateFormat
import java.util.Date

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, Props}
import akka.pattern.ask
import akka.util.Timeout
import models.{Angle, AngleInfo}
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

object WebSocketActor {
  //Ref: http://doc.akka.io/docs/akka/current/scala/actors.html#recommended-practices
  def props(out: ActorRef): Props = Props(new WebSocketActor(out))

  case object FromWebSocketMessage

  case class sendJson(angle: Angle)

  private val formatter = new SimpleDateFormat("dd-MMM-yyyy")

  def dateToString(date: Date): String = formatter.format(date)

  def stringToDate(string: String): Date = formatter.parse(string)


}

class WebSocketActor(out: ActorRef) extends Actor with ActorLogging {
  val angleMarker = "angle"
  implicit val timeout = Timeout(5.seconds)

  val parentActor: ActorSelection = context.actorSelection("akka://application/user/parent-actor")

  def receive = {
    case WebSocketActor.sendJson(angle) =>

      implicit val format = Json.format[Angle]
      val jsonMsg = Json.obj(
        "type" -> angleMarker,
        "message" -> Json.obj("angle" -> angle)
      )
      out ! jsonMsg

    case json: JsValue => log.info("we received json" + json)
      val jsonDate = Try((json \ "date").as[String])
      if (jsonDate.toOption.isDefined) {
        log.info("we received date" + jsonDate.get)
        val date = WebSocketActor.stringToDate(jsonDate.get)
        log.info("we received date as Date " + date)

        val angles: Future[Future[Seq[AngleInfo]]] = (parentActor ? ParentActor.GetAnglesForThis(date)).mapTo[Future[Seq[AngleInfo]]]

        angles.onComplete {
          case Success(a) =>
            log.info("we should received angles")
            a.map { v => log.info("x angle is " + v.head.x) }
          case Failure(e) =>
        }
      }


    case _ => log.info("we might received something")
  }
}