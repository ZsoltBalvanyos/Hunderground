package controllers.security

import java.util.UUID

import com.google.inject.{Inject, Singleton}
import play.api.cache.SyncCacheApi
import cats.implicits._
import util.{AppError, Unauthorised}

@Singleton
class TokenManager @Inject() (cache: SyncCacheApi) {

  def getUser(token: String): Either[AppError, User] = for{
    secret  <- Encryptor.decrypt(token)
    user    <- cache.get[User](secret).toRight(left = Unauthorised)
  } yield user

  def removeUser(token: String): Unit = Encryptor.decrypt(token).map(cache.remove)

  def getToken = UUID.randomUUID.toString

}
