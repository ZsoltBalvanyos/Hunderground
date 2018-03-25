package forms

import play.api.data.Forms._
import play.api.data.Form

object ChangePasswordForm {

  val form = Form(
    mapping(
      "oldpassword" -> nonEmptyText,
      "newpassword" -> nonEmptyText,
      "confirmation" -> nonEmptyText
    )(Data.apply)(Data.unapply)
  )

  case class Data(oldpassword: String,
                  newpassword: String,
                  confirmation: String)
}
