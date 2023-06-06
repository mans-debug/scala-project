package org.itis.mansur
package scalagram.service

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf
import org.itis.mansur.scalagram.models.{Note, Notebook}

package object notes {
  implicit def notebookEncoder: Encoder[Notebook] = deriveEncoder
  implicit def notebookListEncoder: Encoder[List[Notebook]] = deriveEncoder
  implicit def notebookEntityEncoder[F[_]]: EntityEncoder[F, Notebook] = jsonEncoderOf
  implicit def notebookListEntityEncoder[F[_]]: EntityEncoder[F, List[Notebook]] = jsonEncoderOf

  implicit def noteEncoder: Encoder[Note] = deriveEncoder
  implicit def noteListEncoder: Encoder[List[Note]] = deriveEncoder
  implicit def noteEntityEncoder[F[_]]: EntityEncoder[F, Note] = jsonEncoderOf
  implicit def noteListEntityEncoder[F[_]]: EntityEncoder[F, List[Note]] = jsonEncoderOf
}
