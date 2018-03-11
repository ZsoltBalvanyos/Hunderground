package models

import java.time.LocalDate

import play.api.libs.json.Json

case class Event(eventId: Long, location: String, date: LocalDate, description: String)

object Event {
  implicit val eventFormat = Json.format[Event]
}