package org.itis.mansur
package scalagram.models

import cats.effect.IO
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

object RequestForms {
  implicit val addNoteFormDecoder: Decoder[AddNoteForm] = deriveDecoder

  implicit val addNoteFormEntityEncoder: EntityDecoder[IO, AddNoteForm] = jsonOf

  case class AddNoteForm(title: String, content: String)

  implicit val editNoteFormDecoder: Decoder[EditNoteForm] = deriveDecoder

  implicit val editNoteFormEntityEncoder: EntityDecoder[IO, EditNoteForm] = jsonOf

  case class EditNoteForm(content: String)

  implicit val createNotebookFormDecoder: Decoder[CreateNotebookForm] = deriveDecoder

  implicit val createNotebookFormEntityEncoder: EntityDecoder[IO, CreateNotebookForm] = jsonOf

  case class CreateNotebookForm(name: String)

  implicit val editNotebookFormDecoder: Decoder[EditNotebookForm] = deriveDecoder

  implicit val editNotebookFormEntityEncoder: EntityDecoder[IO, EditNotebookForm] = jsonOf

  case class EditNotebookForm(name: String)

  implicit val roleFormDecoder: Decoder[RoleForm] = deriveDecoder

  implicit val roleFormEntityEncoder: EntityDecoder[IO, RoleForm] = jsonOf

  case class RoleForm(role: String)
}
