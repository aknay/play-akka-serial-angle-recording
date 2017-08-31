package actors

/**
  * Created by aknay on 14/6/17.
  */

import com.google.inject.{AbstractModule, Inject}
import play.api.libs.concurrent.AkkaGuiceSupport

//Ref: https://gist.github.com/fancellu/e4e8acdc3d7fd3b9d749352f9d6c68e3
//Ref: https://www.playframework.com/documentation/2.5.x/ScalaAkka
class ActorModule extends AbstractModule with AkkaGuiceSupport {
  def configure {
    bindActor[ParentActor]("parent-actor")
  }
}