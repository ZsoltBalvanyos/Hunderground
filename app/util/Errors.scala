package util

sealed trait AppError

case class EncryptionError(message: String) extends AppError
case class DecryptionError(message: String) extends AppError
case object Unauthorised extends AppError
case object TokenUnavailable extends AppError
