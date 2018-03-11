package controllers

import java.time.LocalDate

import com.google.inject.Inject
import controllers.security.{SecuredAction, SecuredController}
import play.api.cache.SyncCacheApi
import play.api.mvc.{BaseController, ControllerComponents}
import repositories.EventRepository
import services.CalendarService

class EventController @Inject()(cc: ControllerComponents,
                                calendarService: CalendarService,
                                SecuredAction: SecuredAction,
                                cache: SyncCacheApi,
                                eventRepository: EventRepository)
  extends SecuredController(cc, cache) {

  def addRehearsal(date: String, location: String) = SecuredAction {implicit request =>
    implicit val user = request.user

    eventRepository.create(location, getLocalDate(date), "rehearsal")
    Ok
  }

  def addGig(date: String, location: String) = SecuredAction {implicit request =>
    implicit val user = request.user

    eventRepository.create(location, getLocalDate(date), "gig")
    Ok
  }

  def addMemo(date: String, memo: String) = SecuredAction {implicit request =>
    implicit val user = request.user

    eventRepository.create(memo, getLocalDate(date), "memo")
    Ok
  }

  def addHoliday(start: String, person: String, till: String) = SecuredAction {implicit request =>
    implicit val user = request.user

    calendarService.addHoliday(getLocalDate(start), getLocalDate(till), person)
    Ok
  }

  def getLocalDate(date: String) = LocalDate.parse(date, eventRepository.idFormat)

}
