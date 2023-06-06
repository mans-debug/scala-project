package org.itis.mansur
package scalagram.service

import scalagram.dao.TokenRepo
import scalagram.models.User

import cats.data.EitherT
import cats.effect.IO
import doobie.implicits._
import doobie._

import cats.{ApplicativeError, Functor}
import org.itis.mansur.scalagram.models.security.{AccessToken, SecurityError, UserNotFound}

class AccessTokenService(tokenRepo: TokenRepo[IO]) extends SecurityService[IO, AccessToken] {
  override def auth(cred: AccessToken): EitherT[IO, SecurityError, User] =
      tokenRepo
        .getUserByToken(cred)
        .toRight[SecurityError](new UserNotFound)

  override def createCred(user: User): EitherT[IO, SecurityError, AccessToken] =
    EitherT(
      tokenRepo
        .createToken(user)
        .map[Either[SecurityError, AccessToken]](token => Right(token))
        .handleError(_ => Left(new UserNotFound))
    )
}
