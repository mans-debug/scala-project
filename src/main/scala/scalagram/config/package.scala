package org.itis.mansur
package scalagram

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

package object config {
  implicit val dbConfigDecoder: Decoder[DbConfig] = deriveDecoder
  implicit val httpConfigDecoder: Decoder[HttpConfig] = deriveDecoder
  implicit val appConfigDecoder: Decoder[AppConfig] = deriveDecoder
}
