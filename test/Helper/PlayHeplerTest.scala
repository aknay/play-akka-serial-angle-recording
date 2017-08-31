package Helper

import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

//Ref:: https://github.com/hmrc/self-service-time-to-pay-frontend/blob/625c2a6bf313049842fe9e8b356a8bef7038501d/test/uk/gov/hmrc/selfservicetimetopay/controllers/PlayMessagesSpec.scala
/** This is to disable Actor Module while testing */
trait PlayHeplerTest extends PlaySpec with BeforeAndAfterEach with GuiceOneAppPerSuite with ScalaFutures  {
  implicit override lazy val app: Application = new GuiceApplicationBuilder().
    disable[actors.ActorModule].build()
}