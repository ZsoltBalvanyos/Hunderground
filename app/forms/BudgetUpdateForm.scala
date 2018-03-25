package forms

import java.time.LocalDate

import controllers.security.User
import models.Event
import play.api.data.Form
import play.api.data.Forms._

object BudgetUpdateForm {

  def form = Form(
    mapping(
      "entryId"   -> longNumber,
      "event"     -> longNumber,
      "date"      -> default(localDate, LocalDate.now),
      "desc"      -> text,
      "who"       -> number,
      "done"      -> boolean,
      "amount"    -> bigDecimal,
      "direction" -> text
    )(Data.apply)(Data.unapply)
  )



  case class Data(entryId: Long, event: Long, date: LocalDate, desc: String, who: Int, done: Boolean, amount: BigDecimal, direction: String)
}