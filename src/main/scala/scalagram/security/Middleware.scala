package org.itis.mansur
package scalagram.security

import cats.data.{Kleisli, OptionT}
import cats.effect.Sync
import org.http4s.Request
import org.http4s.server.AuthMiddleware
import org.itis.mansur.scalagram.models.User
import org.itis.mansur.scalagram.models.security.AccessToken
import org.itis.mansur.scalagram.service.SecurityService
import org.typelevel.ci.CIString

import java.util.UUID

class Middleware[F[_] : Sync : SecurityService[*[_], AccessToken]] {

  val authUser: Kleisli[OptionT[F, *], Request[F], User] =
    Kleisli(req =>
      getAuthToken(req) match {
        case Some(value) => authToken(value)
        case None => OptionT.none[F, User]
      }
    )

  def getAuthToken(request: Request[F]): Option[String] = {
    request.headers.get(CIString("X-Auth-Token"))
      .map(_.head.value)
  }

  def authToken(token: String): OptionT[F, User] = {
    SecurityService[F, AccessToken].auth(AccessToken(UUID.fromString(token))).toOption
  }

  val authMiddleware: AuthMiddleware[F, User] = AuthMiddleware(authUser)

}
