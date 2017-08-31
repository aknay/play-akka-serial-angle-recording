package controllers

import javax.inject._

import actors._
import akka.actor._
import akka.stream._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.streams.ActorFlow
import play.api.mvc._

import scala.concurrent.Future

/**
  * This class creates the actions and the websocket needed.
  */
@Singleton
class HomeController @Inject()(implicit actorSystem: ActorSystem,
                               mat: Materializer,
                               val messagesApi: MessagesApi)
  extends Controller with I18nSupport {

  // Home page that renders template
  def index = Action.async { implicit request =>
    Future.successful(Ok(views.html.index()))
  }

  def ws: WebSocket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef(out => WebSocketActor.props(out))
  }


}