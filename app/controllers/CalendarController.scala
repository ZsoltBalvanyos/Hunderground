package controllers

import java.util.Calendar
import javax.inject.Inject

import controllers.security.{SecuredAction, SecuredController}
import play.api.cache.SyncCacheApi
import play.api.mvc._
import services.CalendarService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CalendarController @Inject()(val cc: ControllerComponents,
                                   calendarService: CalendarService,
                                   SecuredAction: SecuredAction,
                                   cache: SyncCacheApi)
  extends SecuredController(cc, cache) {

  def getCalendar = SecuredAction.async(parse.defaultBodyParser) { implicit request =>
    implicit val user = request.user

    val cal = Calendar.getInstance()
    val months = calendarService.getCalendar(12, 42, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)

    Future(Ok(views.html.calendar(months)))
  }

}
