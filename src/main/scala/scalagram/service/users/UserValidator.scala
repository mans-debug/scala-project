package org.itis.mansur
package scalagram.security.users

import scalagram.models.User

trait UserValidator {
  def validateLogin(user: User): Either[UserValidateError, User]

  def validatePassword(user: User, password: String): Either[UserValidateError, User]
}

class UserValidatorImpl extends UserValidator {

  def validateLogin(user: User): Either[UserValidateError, User] = user.login match {
    case value if value.isEmpty => Left(EmptyLogin())
    case value if value.length > 30 => Left(TooLongLogin())
    case _ => Right(user)
  }

  def validatePassword(user: User, password: String): Either[UserValidateError, User] = password match {
    case value if value.length < 8 => Left(TooShortPassword())
    case _ => Right(user)
  }

}

class UserValidateError(val message: String) extends Throwable(message)

case class EmptyLogin() extends UserValidateError("login is empty")
case class TooLongLogin() extends UserValidateError("login too long")
case class TooShortPassword() extends UserValidateError("too short password")
case class LoginOccupied() extends UserValidateError("login already exists")
