package util

import java.time.LocalDate

sealed trait AppError

case class EncryptionError(message: String) extends AppError
case class DecryptionError(message: String) extends AppError
case object Unauthorised extends AppError
case object TokenUnavailable extends AppError
case class InvalidDateOrderError(start: LocalDate, finish: LocalDate) extends AppError
