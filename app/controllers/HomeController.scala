package controllers

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject._

import controllers.security.{SecuredAction, SecuredController, User}
import forms.BudgetForm
import models._
import play.api.cache.SyncCacheApi
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.BudgetService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HomeController @Inject()(cc: ControllerComponents,
                               SecuredAction: SecuredAction,
                               budgetService: BudgetService,
                               cache: SyncCacheApi)(implicit ec: ExecutionContext) extends SecuredController(cc, cache) with I18nSupport  {

  def index = Action { implicit request: Request[AnyContent] =>
    implicit val user = request.user
    Ok(views.html.index("Your new application is ready."))
  }
}
