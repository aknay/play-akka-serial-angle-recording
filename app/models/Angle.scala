package models

import java.sql.Timestamp

/**
  * Created by aknay on 14/12/16.
  */

case class Angle(x: Int, y: Int, z: Int)
case class AngleInfo(id: Option[Long]=None, x: Int, y: Int, timeStamp: Timestamp )