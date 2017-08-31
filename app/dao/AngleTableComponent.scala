package dao

import java.sql.Timestamp

import models.AngleInfo
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

  trait AngleTableComponent {
    self: HasDatabaseConfigProvider[JdbcProfile] =>

    import profile.api._
    val angleTableName = "angle_table"
    lazy val angleTableQuery = TableQuery[AngleTable]
    lazy val angleTableQueryInc = angleTableQuery returning angleTableQuery.map(_.id)

    class AngleTable(tag: Tag) extends Table[AngleInfo](tag, angleTableName) {
      val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
      val angleX: Rep[Int] = column[Int]("angle_x")
      val angleY: Rep[Int] = column[Int]("angle_y")
      val timeStamp: Rep[Timestamp] = column[Timestamp]("time_stamp")
      def * = (id.?, angleX,angleY, timeStamp) <> (AngleInfo.tupled, AngleInfo.unapply)
    }

  }