package controllers

import javax.inject._

import actors._
import akka.actor._
import akka.stream._
import dao.AngleDao
import forms.Forms
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsValue
import play.api.libs.streams.ActorFlow
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
/**
  * This class creates the actions and the websocket needed.
  */
@Singleton
class HomeController @Inject()(implicit actorSystem: ActorSystem,
                               mat: Materializer,
                               angleDao: AngleDao,
                               val messagesApi: MessagesApi)
  extends Controller with I18nSupport {

  // Home page that renders template
  def index = Action.async { implicit request =>
    Future.successful(Ok(views.html.index()))
  }

  def time = Action.async { implicit request =>

    for {
      dates <- angleDao.getAllDate
      a <- Future.successful(dates.map(d => (WebSocketActor.dateToString(d), WebSocketActor.dateToString(d))))
    } yield Ok(views.html.time(Forms.dateForm, a))
  }

  def ws: WebSocket = WebSocket.accept[JsValue, JsValue] { request =>
    ActorFlow.actorRef(out => WebSocketActor.props(out))
  }
}

