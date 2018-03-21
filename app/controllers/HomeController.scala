package controllers

import javax.inject._

import controllers.security.{SecuredAction, SecuredController, User}
import forms.SampleForm
import play.api.cache.SyncCacheApi
import play.api.i18n.I18nSupport
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class HomeController @Inject()(cc: ControllerComponents,
                               SecuredAction: SecuredAction,
                               cache: SyncCacheApi)(implicit ec: ExecutionContext) extends SecuredController(cc, cache) with I18nSupport  {

  def index = SecuredAction { implicit request: Request[AnyContent] =>
    implicit val user = request.user
    Ok(views.html.index("Your new application is ready."))
  }

  def sampleForm = Action {implicit request =>
    implicit val user: Option[User] = None
    Ok(views.html.sample(forms.SampleForm.form))
  }

  def sample = Action { implicit request =>
    SampleForm.form.bindFromRequest.fold(
      error => println(s"error: $error"),
      data  => println(s"Data: ${data.text}, ${data.date}, ${data.n}")
    )
    Ok
  }

}
