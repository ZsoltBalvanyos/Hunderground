package models

import java.time.format.DateTimeFormatter
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
case class Holiday(eventId: Long, date: LocalDate, userId: Int, start: LocalDate, finish: LocalDate) extends Event
case class Gig(eventId: Long, date: LocalDate, location: String) extends Event
case class Rehearsal(eventId: Long, date: LocalDate, location: String, start: LocalTime, finish: LocalTime) extends Event {

  val dtf = DateTimeFormatter.ofPattern("hh:mm:ss")

  override def equals(obj: scala.Any): Boolean = {
    if(!obj.isInstanceOf[Rehearsal]) return false

    val other: Rehearsal = obj.asInstanceOf[Rehearsal]

    if(this.location != other.location) return false
    if(this.date != other.date) return false
    if(this.start.format(dtf) != other.start.format(dtf)) return false
    if(this.finish.format(dtf) != other.finish.format(dtf)) return false
    this.eventId == other.eventId
  }
}

case class CalendarEntry(legend: String, colour: String, event: Event)

case class HolidayDisplay(name: String, start: LocalDate, finish: LocalDate)
