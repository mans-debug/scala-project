package org.itis.mansur
package scalagram.models

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

package object security {
  implicit def accessTokenDecoder: Decoder[AccessToken] = deriveDecoder

  implicit def accessTokenEncoder: Encoder[AccessToken] = deriveEncoder

  implicit def accessTokenEntityEncoder[F[_]]: EntityEncoder[F, AccessToken] = jsonEncoderOf
}
