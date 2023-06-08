package org.itis.mansur
package scalagram.routes

import scalagram.models.RequestForms._
import scalagram.models.{Note, Role, User}

import cats.effect.IO
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.http4s.{AuthedRoutes, EntityEncoder, Response}
import org.itis.mansur.scalagram.models.security.AccessError
import org.itis.mansur.scalagram.service.notes.{NotebookService, noteEntityEncoder, noteListEntityEncoder, notebookEntityEncoder, notebookListEntityEncoder}
import tofu.syntax.handle._

class NotesEndpoints(notebookService: NotebookService[IO]) extends Http4sDsl[IO] {
  def notesEndpoints(): AuthedRoutes[User, IO] = AuthedRoutes.of[User, IO] {
    case GET -> Root / userId / "notebooks" as user =>
      handleAccessRequest(notebookService.getUserNotebooks(User(userId.toLong, "", "")))

    case GET -> Root / notebookId / "notes" as user =>
      handleAccessRequest(notebookService.getNotebookNotes(user, notebookId.toLong))

    case req@POST -> Root / notebookId / "addNote" as user =>
      handleAccessRequest(req.req.
        as[AddNoteForm]
        .flatMap(value =>
          notebookService
            .addNote(user, Note(0, value.title, notebookId.toLong, value.content))))

    case DELETE -> Root / notebookId / "notes" / noteId / "delete" as user =>
      handleAccessRequest(notebookService.deleteNote(user, Note(noteId.toLong, "", notebookId.toLong, "")))

    case req@PATCH -> Root / notebookId / "notes" / noteId / "edit" as user =>
      handleAccessRequest(req.req.
        as[EditNoteForm]
        .flatMap(value => notebookService.editNote(user, Note(noteId.toLong, "", notebookId.toLong, value.content))))

    case req@POST -> Root / "notebooks" / "create" as user =>
      handleAccessRequest(req.req.
        as[CreateNotebookForm]
        .flatMap(value => notebookService.create(user, value.name)))

    case req@PATCH -> Root / notebookId / "edit" as user =>
      handleAccessRequest(req.req.
        as[EditNotebookForm]
        .flatMap(value => notebookService.edit(user, notebookId.toLong, value.name)))

    case DELETE -> Root / notebookId / "delete" as user =>
      handleAccessRequest(notebookService.delete(user, notebookId.toLong))

    case req@POST -> Root / notebookId / "addRoleTo" / userId as user =>
      handleAccessRequest(req.req.
        as[EditNotebookForm]
        .flatMap(value =>
          notebookService
            .changeRole(user, User(userId.toLong, "", ""), notebookId.toLong, Role.fromStr(value.name))))

    case DELETE -> Root / notebookId / "removeRoleOf" / userId as user =>
      handleAccessRequest(notebookService.removeRole(user, User(userId.toLong, "", ""), notebookId.toLong))
  }
  def handleAccessRequest[A](f: => IO[A])(implicit encoder: EntityEncoder[IO, A]): IO[Response[IO]] = {
    f.attempt.flatMap {
      case Left(value) => Forbidden()
      case Right(value) => Ok(value)
    }
      .handleWith[NumberFormatException](_ => UnprocessableEntity())
  }
  
}
