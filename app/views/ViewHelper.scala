package views

import models._

object ViewHelper {

  def getEvent(event: Event) = event match {
    case Gig(eventId, date, location) => s"editGig('$eventId', '$date', '$location')"
    case Memo(eventId, date, memo)    => s"editMemo('$eventId', '$date', '$memo')"
    case Holiday(eventId, date, userId, start, finish) => s"editHoliday('$eventId', '$date', '$userId', '$start', '$finish')"
    case Rehearsal(eventId, date, location, start, finish) => s"editRehearsal('$eventId', '$date', '$location', '$start', '$finish')"
  }
}
