package org.itis.mansur
package scalagram.builders

import scalagram.config.AppConfig
import scalagram.dao._
import scalagram.routes.{NotesEndpoints, SecurityEndpoints}
import scalagram.security.Middleware
import scalagram.security.users.{UserService, UserValidatorImpl}
import scalagram.service.notes.NotebookService
import scalagram.service.{AccessTokenService, SecurityService}

import cats.Functor
import cats.effect._
import cats.implicits.toSemigroupKOps
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.{Router, Server}
import org.itis.mansur.scalagram.models.security.Password
import tofu.logging.Logging

class ServerBuilder(implicit loggingMake: Logging.Make[IO], sync: Sync[IO]) {

  private implicit val logging: Logging[IO] = Logging.Make[IO].byName("Server")

  def buildServer: Resource[IO, Server] =
    for {
      conf <- AppConfig.getConfig
      _ <- Resource.eval(DatabaseServicesBuilder.migrateDatabase(conf.db))
      transactor <- DatabaseServicesBuilder.transactor[IO](conf.db)
      userRepo = UserRepo.make
      tokenRepo = TokenRepo.make(transactor)
      securityService = SecurityService.make(userRepo, transactor)
      accessTokenService = new AccessTokenService(tokenRepo)
      userService = UserService.make(userRepo, transactor)
      userValidator = new UserValidatorImpl
      securityEndpoints = new SecurityEndpoints(userValidator, securityService, accessTokenService, userService)
      notebookService = NotebookService.make(NoteRepo.make, NotebookRepo.make, RoleRepo.make, transactor)
      authMiddleware = new Middleware(accessTokenService).authMiddleware
      notesEndpoints = new NotesEndpoints(notebookService)
      httpApp = Router(
        "/" -> (securityEndpoints.getEndpoints <+> authMiddleware(notesEndpoints.notesEndpoints()))
      ).orNotFound
      server <- BlazeServerBuilder[IO]
        .bindHttp(port = conf.http.port)
        .withHttpApp(httpApp)
        .resource
    } yield server


}
