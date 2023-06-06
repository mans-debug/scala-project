package org.itis.mansur
package scalagram.routes

import cats.data.EitherT
import cats.effect.IO
import cats.effect.kernel.Concurrent
import cats.implicits.{toFlatMapOps, toSemigroupKOps}
import cats.{Applicative, Functor, Monad}
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import org.http4s.FormDataDecoder.formEntityDecoder
import org.http4s.circe.jsonOf
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}
import org.itis.mansur.scalagram.models.User
import org.itis.mansur.scalagram.models.security.{AccessToken, Password, SecurityError}
import org.itis.mansur.scalagram.security.PasswordEncode.PasswordEncoderOps
import org.itis.mansur.scalagram.security.users.{LoginOccupied, UserService, UserValidateError, UserValidator}
import org.itis.mansur.scalagram.service.SecurityService
import org.postgresql.util.PSQLException
import org.typelevel.ci.CIString
import tofu.Handle
import tofu.logging.ServiceLogging.byUniversal
import tofu.logging._
import tofu.syntax.handle.HandleOps
import tofu.syntax.logging._

import java.util.UUID

class SecurityEndpoints(userValidator: UserValidator,
                        securityServicePass: SecurityService[IO, Password],
                        securityServiceAccessToken: SecurityService[IO, AccessToken],
                        userService: UserService[IO]) extends Http4sDsl[IO] {

  def signUpEndpoint: HttpRoutes[IO] = {
    HttpRoutes.of[IO] {
      case req@POST -> Root / "signUp" => req
        .as[SignUpForm]
        .flatMap(processSignUpForm(_).value)
        .handleWith(
          psqlHandler[IO, PSQLException])
        .flatMap {
          case Left(value) => BadRequest(value.message)
          case Right(_) => Ok()
        }
    }
  }

  def signInEndpoint: HttpRoutes[IO] = {
    HttpRoutes.of[IO] {
      case req@POST -> Root / "signIn" => req
        .as[SignInForm]
        .flatMap(processSignInForm(_).value)
        .flatMap {
          case Left(value) => BadRequest(value.message)
          case Right(value) => Ok(value)
        }
    }
  }

  def authCheckEndpoint: HttpRoutes[IO] = {
    HttpRoutes.of[IO] {
      case req@GET -> Root / "authCheck" => req.headers.get(CIString("X-Auth-Token"))
        .map(_.head.value)
        .map(token => {
          securityServiceAccessToken.auth(AccessToken(UUID.fromString(token))).value
            .flatMap({
              case Left(value) => BadRequest(value.message)
              case Right(value) => Ok()
            })
        }).getOrElse(BadRequest("No token"))
    }
  }

  def psqlHandler[F0[_] : Applicative, E]: E => F0[Either[UserValidateError, Unit]] =
    _ => EitherT.leftT[F0, Unit](LoginOccupied().asInstanceOf[UserValidateError]).value


  def processSignInForm(form: SignInForm): EitherT[IO, SecurityError, AccessToken] = {
    securityServicePass
      .auth(Password(form.login, form.password))
      .flatMap(securityServiceAccessToken.createCred(_))
  }

  def processSignUpForm(form: SignUpForm): EitherT[IO, UserValidateError, Unit] = {
    EitherT.fromEither(
      userValidator
        .validateLogin(User(0, form.login, ""))
        .flatMap(userValidator.validatePassword(_, form.password))
    ).flatMap(user =>
      EitherT.right(
        form.password
          .encode[IO]
          .map(hash => user.copy(passwordHash = hash))
          .flatMap(userService.saveUser)
          .flatMap(_ => info"New user added: $user"))
    )
  }

  case class SignUpForm(login: String, password: String)

  case class SignInForm(login: String, password: String)

  implicit val signUpFormDecoder: Decoder[SignUpForm] = deriveDecoder

  implicit val signInFormDecoder: Decoder[SignInForm] = deriveDecoder

  implicit val signUpFormEntityDecoder: EntityDecoder[IO, SignUpForm] = jsonOf

  implicit val signInFormEntityDecoder: EntityDecoder[IO, SignInForm] = jsonOf

  def getEndpoints: HttpRoutes[IO] = {
    signUpEndpoint <+> signInEndpoint <+> authCheckEndpoint
  }

}
