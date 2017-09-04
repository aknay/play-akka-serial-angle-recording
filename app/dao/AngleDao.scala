package dao

import java.sql.Timestamp
import java.util.Date
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
class AngleDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends AngleTableComponent with HasDatabaseConfigProvider[JdbcProfile] {

  /** describe the structure of the tables: */
  /** Note: table cannot be named as 'user', otherwise we will problem with Postgresql */

  import profile.api._

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

  def insert(angle: Angle): Future[Long] =  {
    val angleInfo = AngleInfo(x = angle.x, y = angle.y, timeStamp = getTimeStampFromDate(new Date()))
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

}