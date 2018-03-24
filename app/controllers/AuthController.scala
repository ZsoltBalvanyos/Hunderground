package controllers

import javax.inject.Inject

import controllers.security._
import forms.{SignInForm, SignUpForm}
import play.api.cache.SyncCacheApi
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories.UserRepository
import util.Constant

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class AuthController @Inject()(cc: ControllerComponents,
                               userRepo: UserRepository,
                               tokenManager: TokenManager,
                               cache: SyncCacheApi)(implicit ec: ExecutionContext)
  extends AbstractController(cc)
  with I18nSupport {

  implicit val user: Option[User] = None

  def goodResponse(nextToken: String)(implicit request: Request[AnyContent]) = {

    val uri = request
      .cookies.get(Constant.playSession)
      .flatMap(cookie => cache.get[String](s"${cookie.value}-${Constant.afterLogin}"))
      .getOrElse("/getCalendar")

    Future.successful(Redirect(uri, 301).withCookies(Cookie(Constant.tokenName, nextToken, Some(60 * 60 * 24 * 90))))
  }

  def badResponse(message: String) =
    Future.successful(Redirect(routes.AuthController.signIn()).flashing("error" -> message))

  def signInForm = Action.async { implicit request =>
    Future.successful(Ok(views.html.signIn(SignInForm.form)))
  }

  def signIn = Action.async { implicit request =>

    SignInForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.signIn(form))),
      data => {
        userRepo.findByEmailAddress(data.email) flatMap {
          case None =>
            Future.successful(Redirect(routes.AuthController.signIn).flashing("error" -> s"User with email address ${data.email} does not exists"))
          case Some(user) => {
            if(PasswordManager.hash(user.salt + data.password) == user.password) {
              val nextToken = tokenManager.getToken
              cache.set(nextToken, user, 90 days)
              Encryptor.encrypt(nextToken)
                .fold(
                  error => badResponse(error.message),
                  goodResponse
                )

            } else {
              badResponse("Invalid password")
            }
          }
        }
      }
    )
  }

  def signUpForm = Action.async { implicit request =>
    Future.successful(Ok(views.html.signUp(SignUpForm.form)))
  }

  def signUp = Action.async { implicit request =>

    SignUpForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.signUp(form))),
      data => {
        userRepo.findByEmailAddress(data.email) flatMap {
          case Some(user) =>
            Future.successful(Redirect(routes.AuthController.signUp()).flashing("error" -> s"User with email address ${user.email} exists"))
          case None =>
            registerUser(data)
        }
      }
    )
  }

  def signOut = Action.async { implicit request =>

    request.cookies.get(Constant.tokenName).map(cookie => tokenManager.removeUser(cookie.value))
    Future.successful(Redirect(routes.AuthController.signIn()).withNewSession.flashing(
      "success" -> "You've been logged out"
    ))
  }

  private def registerUser(data: SignUpForm.Data) = {
    val salt = PasswordManager.salt
    val hashedPassword = PasswordManager.hash(s"$salt${data.password}")
    userRepo.create(data.firstName, data.lastName, data.email, hashedPassword, salt, None)
    Future.successful(Redirect(routes.AuthController.signIn))
  }
}
