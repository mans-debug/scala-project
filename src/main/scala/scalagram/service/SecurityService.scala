package org.itis.mansur
package scalagram.service

import scalagram.dao.UserRepo
import scalagram.models.User
import scalagram.models.security.{CredNotValid, Password, PasswordCreationNotSupported, SecurityError, UserNotFound}
import scalagram.security.PasswordEncode

import cats.data.EitherT
import cats.effect.IO
import doobie.implicits._
import doobie.{ConnectionIO, Transactor}


trait SecurityService[F[_], Cred] {
  def auth(cred: Cred): EitherT[F, SecurityError, User]

  def createCred(user: User): EitherT[F, SecurityError, Cred]
}

object SecurityService {
  private class Impl(userRepo: UserRepo[ConnectionIO], xa: Transactor[IO]) extends SecurityService[IO, Password] {
    override def auth(cred: Password): EitherT[IO, SecurityError, User] = {
      EitherT(
        userRepo.getByLogin(cred.login).transact(xa)
          .map(checkPassword(_, cred))
      )
    }

    override def createCred(user: User): EitherT[IO, SecurityError, Password] = EitherT.fromEither(Left(new PasswordCreationNotSupported))

    def checkPassword(user: Option[User], password: Password): Either[SecurityError, User] = user match {
      case None => Left(new UserNotFound)
      case Some(value) if PasswordEncode[IO].check(password.password, value.passwordHash) => Right(value)
      case Some(_) => Left(new CredNotValid)
    }

  }

  def make(userRepo: UserRepo[ConnectionIO], xa: Transactor[IO]): SecurityService[IO, Password] = new Impl(userRepo, xa)
}
