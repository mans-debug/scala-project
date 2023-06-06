package org.itis.mansur
package scalagram.security

import cats.effect.kernel.Sync
import com.github.t3hnar.bcrypt._

trait PasswordEncode[F[_]] {
  def encode(password: String): F[String]

  def check(password: String, passwordHash: String): Boolean
}

object PasswordEncode {

  def apply[F[_]](implicit pe: PasswordEncode[F]): PasswordEncode[F] = pe

  implicit class PasswordEncoderOps(value: String) {
    def encode[F[_]](implicit pe: PasswordEncode[F]): F[String] = pe.encode(value)

    def check[F[_]](passwordHash: String)(implicit pe: PasswordEncode[F]): Boolean = pe.check(value, passwordHash)
  }

  implicit def defaultPasswordEncoder[F[_] : Sync]: PasswordEncode[F] = new PasswordEncode[F] {
    override def encode(password: String): F[String] = Sync[F].delay(password.boundedBcrypt)

    override def check(password: String, passwordHash: String): Boolean = password.isBcryptedBounded(passwordHash)
  }
}
