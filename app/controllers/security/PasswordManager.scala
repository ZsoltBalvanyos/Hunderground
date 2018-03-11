package controllers.security

import java.security.{MessageDigest, SecureRandom}

import com.google.common.io.BaseEncoding

object PasswordManager {

  private val base64 = BaseEncoding.base64

  def hash(password: String): String =
    base64.encode(
      MessageDigest
        .getInstance("MD5")
        .digest(password.getBytes))

  def salt: String = {
    val s = new SecureRandom
    val b = new Array[Byte](16)
    s.nextBytes(b)
    base64.encode(b).dropRight(2)
  }
}
