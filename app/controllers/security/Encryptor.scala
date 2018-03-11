package controllers.security

import javax.crypto.{Cipher, KeyGenerator}

import com.google.common.io.BaseEncoding
import util.{DecryptionError, EncryptionError}

import scala.util.{Failure, Success, Try}

object Encryptor {

  private val algorithm     = "DES"
  private val mode          = "ECB"
  private val padding       = "PKCS5Padding"
  private val cipherConfig  = s"$algorithm/$mode/$padding"

  private val keyGenerator = KeyGenerator.getInstance(algorithm)
  private val key = keyGenerator.generateKey

  private val encryptor =  Cipher.getInstance(cipherConfig)
  encryptor.init(Cipher.ENCRYPT_MODE, key)

  private val decryptor =  Cipher.getInstance(cipherConfig)
  decryptor.init(Cipher.DECRYPT_MODE, key)

  private val base64 = BaseEncoding.base64

  def encrypt(value: String) = Try(base64.encode(encryptor.doFinal(value.getBytes))) match {
    case Success(result) => Right(result)
    case Failure(error)  => Left(EncryptionError(error.getMessage))
  }

  def decrypt(value: String) = Try(new String(decryptor.doFinal(base64.decode(value)))) match {
    case Success(result) => Right(result)
    case Failure(error)  => Left(DecryptionError(error.getMessage))
  }

}

