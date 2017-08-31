package ModelTest

/**
  * Created by aknay on 4/4/17.
  */

import Helper.PlayHeplerTest
import dao.AngleDao
import models.Angle

class AngleDaoTest extends PlayHeplerTest {
  val angleDao: AngleDao = app.injector.instanceOf(classOf[AngleDao])

  def deleteAllEntry: Unit = {
    val angleInfoList = angleDao.getAll.futureValue
    angleInfoList.foreach(v => angleDao.delete(v.id.get))
  }

  override def beforeEach(): Unit = {
    deleteAllEntry
  }

  override def afterEach(): Unit = {
    deleteAllEntry
  }

  "should add angle" in {
    val angle = Angle(10, 20, 30)
    val id = angleDao.insert(angle).futureValue
    val insertedAngle = angleDao.get(id).futureValue
    insertedAngle.get.x mustBe 10
    insertedAngle.get.y mustBe 20
  }
}