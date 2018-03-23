package models

object SongStatus {
  def fromString(value: String): Either[String, SongStatus] = value match {
    case "ready" => Right(Ready)
    case "backlog" => Right(Backlog)
    case _ => Left(s"'$value' is not a valid status")
  }
}


sealed trait SongStatus
case object Ready extends SongStatus
case object Backlog extends SongStatus
