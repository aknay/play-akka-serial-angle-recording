package dao

import java.sql.Timestamp
import java.util.{Calendar, Date}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}

import models.{Angle, AngleInfo}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.meta.MTable

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._


/**
  * Created by aknay on 31/8/17.
  */

@Singleton()
class AngleDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

  /** describe the structure of the tables: */
  /** Note: table cannot be named as 'user', otherwise we will problem with Postgresql */

  import profile.api._

  val angleTableName = "angle_table"
  lazy val angleTableQuery = TableQuery[AngleTable]
  lazy val angleTableQueryInc = angleTableQuery returning angleTableQuery.map(_.id)

  implicit val dateMapper = MappedColumnType.base[java.util.Date, java.sql.Timestamp](
    d => new java.sql.Timestamp(d.getTime),
    d => new java.util.Date(d.getTime))

  class AngleTable(tag: Tag) extends Table[AngleInfo](tag, angleTableName) {
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    val angleX: Rep[Int] = column[Int]("angle_x")
    val angleY: Rep[Int] = column[Int]("angle_y")
    val timeStamp: Rep[java.util.Date] = column[java.util.Date]("time_stamp")

    def * = (id.?, angleX, angleY, timeStamp) <> (AngleInfo.tupled, AngleInfo.unapply)
  }

  /** The following statements are Action */
  private lazy val createTableAction = angleTableQuery.schema.create
  val defaultAngle = AngleInfo(id = None, 361, 361, getTimeStampFromDate(new Date()))
  createTableIfNotExisted()

  /** Ref: http://slick.lightbend.com/doc/3.0.0/database.html */

  //This is the blocking method with maximum waiting time of5 seconds
  //This is also helper method for DBIO
  private def blockExec[T](action: DBIO[T]): T = Await.result(db.run(action), 5 seconds)

  private def getTimeStampFromDate(date: Date): Timestamp = new Timestamp(date.getTime)

  def createTableIfNotExisted() {
    val x = blockExec(MTable.getTables(angleTableName)).toList
    if (x.isEmpty) {
      blockExec(createTableAction)
    }
  }


  def insert(angle: Angle): Future[Long] = {
    val angleInfo = AngleInfo(x = angle.x, y = angle.y, date = getTimeStampFromDate(new Date()))
    db.run(angleTableQuery returning angleTableQuery.map(_.id) += angleInfo)
  }

  val d1: Date = new java.util.Date(java.sql.Date.valueOf("2012-12-24").getTime)

  def insert(angle: Angle, date: Date) = {
    val angleInfo = AngleInfo(x = angle.x, y = angle.y, date = date)
    db.run(angleTableQuery returning angleTableQuery.map(_.id) += angleInfo)
  }

  def delete(id: Long): Future[Unit] = {
    db.run(angleTableQuery.filter(_.id === id).delete).map { _ => () }
  }

  def get(id: Long): Future[Option[AngleInfo]] = {
    db.run(angleTableQuery.filter(_.id === id)
      .result.headOption)
  }

  def getAll: Future[Seq[AngleInfo]] = {
    db.run(angleTableQuery.result)
  }

  def getLatestEntry: Future[AngleInfo] = {
    getAll.map(x => if (x.nonEmpty) x.last else defaultAngle)
  }

  def getByDate(date: Date): Future[Seq[AngleInfo]] = {
    val dateWithoutTime = removeTimeInfoFromDate(date)
    val dateCloseToNextDay = new Date(dateWithoutTime.getTime + TimeUnit.DAYS.toMillis(1) - 1)
    db.run(angleTableQuery.filter(_.timeStamp >= dateWithoutTime).filter(_.timeStamp < dateCloseToNextDay).result)
  }

  private def removeTimeInfoFromDate(date: Date): Date = {
    val cal = Calendar.getInstance()
    cal.setTime(date)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    cal.getTime
  }
}