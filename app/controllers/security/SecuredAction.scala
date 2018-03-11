package controllers.security

import com.google.inject.Inject
import controllers.routes
import play.api.cache.SyncCacheApi
import play.api.mvc.Results._
import play.api.mvc._
import util.{Constant, TokenUnavailable}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class SecuredAction @Inject()(parser: BodyParsers.Default, cache: SyncCacheApi, tokenManager: TokenManager)(implicit ec: ExecutionContext) extends ActionBuilderImpl(parser) {

  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {


    val playSession = request.cookies.get(Constant.playSession) match {
      case Some(cookie) => cookie.value
      case None         => ""
    }

    def redirectToSignIn = {
      cache.set(s"$playSession-${Constant.afterLogin}", request.uri)
      Future(Redirect(routes.AuthController.signIn))
    }

    request
      .cookies
      .get(Constant.tokenName)
      .map(_.value)
      .toRight(TokenUnavailable)
      .flatMap(token => {
        val user = tokenManager.getUser(token)
        cache.remove(token)
        user})
      .map(user => {
        val nextToken = tokenManager.getToken
        cache.set(nextToken, user, 90 days)
        cache.set(s"$nextToken-${Constant.afterLogin}", request.uri, 1 minute)
        Encryptor
          .encrypt(nextToken)
          .fold(
            _          => redirectToSignIn,
            encrypted  => block(request).map(_.withCookies(Cookie(Constant.tokenName, encrypted)))
          )})
      .getOrElse(redirectToSignIn)

  }
}
