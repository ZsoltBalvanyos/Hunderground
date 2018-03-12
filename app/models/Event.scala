package models

import java.time.{LocalDate, LocalTime}

import play.api.libs.json.Json

object Memo {
  implicit val memoFormat = Json.format[Memo]
}

object Gig {
  implicit val gigFormat = Json.format[Gig]
}

object Rehearsal {
  implicit val rehearsalFormat = Json.format[Rehearsal]
}

object Holiday {
  implicit val holidayFormat = Json.format[Holiday]
}

sealed trait Event {
  val eventId: Long
  val date: LocalDate
}

case class Memo(eventId: Long, date: LocalDate, memo: String) extends Event
case class Rehearsal(eventId: Long, date: LocalDate, location: String, start: LocalTime, finish: LocalTime) extends Event
case class Holiday(eventId: Long, date: LocalDate, userId: Int) extends Event
case class Gig(eventId: Long, date: LocalDate, location: String) extends Event

case class CalendarEntry(legend: String, colour: String)