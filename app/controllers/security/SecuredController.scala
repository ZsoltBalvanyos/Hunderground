package controllers.security

import javax.inject.Inject

import play.api.cache.SyncCacheApi
import play.api.mvc.{AbstractController, ControllerComponents, Request}
import util.{Constant, DecryptionError}

class SecuredController @Inject()(cc: ControllerComponents, cache: SyncCacheApi) extends AbstractController(cc) {

  implicit class RichRequest[A](request: Request[A]) {

    implicit def user =
      request
        .cookies
        .get(Constant.tokenName)
        .flatMap(cookie => Encryptor.decrypt(cookie.value).toOption)
        .flatMap(secret => cache.get[User](secret))
  }

}
