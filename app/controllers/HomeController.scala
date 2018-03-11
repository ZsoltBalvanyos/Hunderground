package controllers

import javax.inject._

import controllers.security.{SecuredAction, SecuredController}
import play.api.cache.SyncCacheApi
import play.api.mvc._

@Singleton
class HomeController @Inject()(cc: ControllerComponents,
                               SecuredAction: SecuredAction,
                               cache: SyncCacheApi)
  extends SecuredController(cc, cache) {

  def index = SecuredAction { implicit request: Request[AnyContent] =>
    implicit val user = request.user
    Ok(views.html.index("Your new application is ready."))
  }



}
