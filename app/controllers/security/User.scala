package controllers.security

case class User(userID: Int,
                firstName: Option[String],
                lastName: Option[String],
                email: String,
                password: String,
                salt: String,
                avatarURL: Option[String])
