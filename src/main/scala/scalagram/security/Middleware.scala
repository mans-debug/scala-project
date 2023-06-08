package org.itis.mansur
package scalagram.security

import cats.data.{Kleisli, OptionT}
import cats.effect.{IO, Sync}
import org.http4s.Request
import org.http4s.server.AuthMiddleware
import org.itis.mansur.scalagram.models.User
import org.itis.mansur.scalagram.models.security.AccessToken
import org.itis.mansur.scalagram.service.SecurityService
import org.typelevel.ci.CIString

import java.util.UUID

class Middleware(securityService: SecurityService[IO, AccessToken]){

  val authUser: Kleisli[OptionT[IO, *], Request[IO], User] =
    Kleisli(req =>
      getAuthToken(req) match {
        case Some(value) => authToken(value)
        case None => OptionT.none[IO, User]
      }
    )

  def getAuthToken(request: Request[IO]): Option[String] = {
    request.headers.get(CIString("X-Auth-Token"))
      .map(_.head.value)
  }

  def authToken(token: String): OptionT[IO, User] = {
    securityService.auth(AccessToken(UUID.fromString(token))).toOption
  }

  val authMiddleware: AuthMiddleware[IO, User] = AuthMiddleware(authUser)

}
