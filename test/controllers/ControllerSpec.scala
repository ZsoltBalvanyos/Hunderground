package controllers

import org.scalatest.{FlatSpec, Matchers}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc._

class ControllerSpec extends FlatSpec with Matchers {

  it should "work" in {

    val service = new BaseController {

      override protected def controllerComponents: ControllerComponents = new GuiceApplicationBuilder().injector().instanceOf[ControllerComponents]

      val logoff: Action[AnyContent] = Action { implicit request =>
        Ok
      }
    }

  }

}
