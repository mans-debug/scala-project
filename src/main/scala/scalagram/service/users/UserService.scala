package org.itis.mansur
package scalagram.security.users

import scalagram.models.User

import cats.effect.IO
import org.itis.mansur.scalagram.dao.UserRepo
import doobie.implicits._
import doobie._

import scala.tools.nsc.doc.html.HtmlTags.Tr

trait UserService[F[_]] {
  def saveUser(user: User): F[User]
}

object UserService {
  private class Impl(userRepo: UserRepo[ConnectionIO], xa: Transactor[IO]) extends UserService[IO] {
    override def saveUser(user: User): IO[User] = userRepo.save(user).transact(xa)
  }

  def make(userRepo: UserRepo[ConnectionIO], xa: Transactor[IO]): UserService[IO] = new Impl(userRepo, xa)
}
