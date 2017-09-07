package actors

/**
  * Created by aknay on 8/6/17.
  */

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.util.Timeout
import models.Angle
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.duration._

object WebSocketActor {
  //Ref: http://doc.akka.io/docs/akka/current/scala/actors.html#recommended-practices
  def props(out: ActorRef): Props = Props(new WebSocketActor(out))

  case object FromWebSocketMessage

  case class sendJson(angle: Angle)

}

class WebSocketActor(out: ActorRef) extends Actor with ActorLogging {
  val angleMarker = "angle"
  implicit val timeout = Timeout(5.seconds)

  def receive = {
    case WebSocketActor.sendJson(angle) =>

      implicit val format = Json.format[Angle]
      val jsonMsg = Json.obj(
        "type" -> angleMarker,
        "message" -> Json.obj("angle" -> angle)
      )
      out ! jsonMsg

    case json: JsValue => log.info("we received json" + json)

    case _ => log.info("we might received something")
  }
}