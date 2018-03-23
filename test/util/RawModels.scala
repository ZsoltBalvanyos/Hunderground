package util

import java.time.{LocalDate, LocalTime}

import models.SongStatus

trait RawModels {

  

  case class RawSong(artist: String, title: String, status: SongStatus, key: String)

  case class RawMemo(date: LocalDate, memo: String)
  case class RawRehearsal(date: LocalDate, location: String, start: LocalTime, finish: LocalTime)
  case class RawHoliday(date: LocalDate, userId: Int, start: LocalDate, finish: LocalDate)
  case class RawGig(date: LocalDate, location: String)

  case class RawUser(firstName: Option[String],
                     lastName: Option[String],
                     email: String,
                     password: String,
                     salt: String,
                     avatarURL: Option[String])
}
