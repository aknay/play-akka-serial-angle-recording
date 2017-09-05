package ModelTest

/**
  * Created by aknay on 4/4/17.
  */

import java.util.Date

import Helper.PlayHeplerTest
import dao.AngleDao
import models.Angle

class AngleDaoTest extends PlayHeplerTest {
  val angleDao: AngleDao = app.injector.instanceOf(classOf[AngleDao])

  def deleteAllEntry(): Unit = {
    val angleInfoList = angleDao.getAll.futureValue
    angleInfoList.foreach(v => angleDao.delete(v.id.get))
  }

  override def beforeEach(): Unit = {
    angleDao.createTableIfNotExisted()
    deleteAllEntry()
  }

  override def afterEach(): Unit = {
    deleteAllEntry()
  }

  "should add angle" in {
    val angle = Angle(10, 20, 30)
    val id = angleDao.insert(angle).futureValue
    val insertedAngle = angleDao.get(id).futureValue
    insertedAngle.get.x mustBe 10
    insertedAngle.get.y mustBe 20
  }

  "should get latest entry angle" in {
    val angleOne = Angle(1, 2, 3)
    val angleTwo = Angle(4, 5, 6)
    val angleThree = Angle(7, 8, 9)

    angleDao.insert(angleOne).futureValue
    angleDao.insert(angleTwo).futureValue
    angleDao.insert(angleThree).futureValue

    val angleInfo = angleDao.getLatestEntry.futureValue
    angleInfo.x mustBe angleThree.x
    angleInfo.y mustBe angleThree.y
  }

  "should get default latest entry angle when there is none" in {
    val angleInfo = angleDao.getLatestEntry.futureValue
    angleInfo.x mustBe angleDao.defaultAngle.x
    angleInfo.y mustBe angleDao.defaultAngle.y
  }


  "should get data from a date" in {
    val oldDate = new java.util.Date(java.sql.Date.valueOf("2012-12-24").getTime)

    angleDao.insert(Angle(0, 1, 2), new Date()).futureValue
    angleDao.insert(Angle(3, 4, 5), new Date()).futureValue
    angleDao.insert(Angle(6, 7, 8), oldDate).futureValue

    val allAngleList = angleDao.getAll.futureValue
    allAngleList.size mustBe 3

    val todayDataList = angleDao.getByDate(new Date()).futureValue
    todayDataList.size mustBe 2
  }


}