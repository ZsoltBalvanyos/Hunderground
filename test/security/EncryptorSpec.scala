package security

import controllers.security.Encryptor
import org.scalatest.{FlatSpec, Matchers}

class EncryptorSpec extends FlatSpec with Matchers {

  "Encryptor" should "encrypt a string and decrypt to the original value" in {
    val text = "MyVerySecretPassword"
    Encryptor.encrypt(text).flatMap(Encryptor.decrypt) shouldBe Right(text)
  }
}
