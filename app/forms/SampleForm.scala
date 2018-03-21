package forms

import java.time.{LocalDate, LocalTime}

import play.api.data.Form
import play.api.data.Forms._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{BaseController, ControllerComponents}

object SampleForm {

  val form = Form(
    mapping(
      "text" -> text,
      "date" -> localDate,
      "time" -> number(min = 0, max = 12, strict = true)
    )(Data.apply)(Data.unapply)
  )

  case class Data(text: String, date: LocalDate, n: Int)


}
