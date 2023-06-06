package org.itis.mansur
package scalagram.dao

import scalagram.models.User
import scalagram.models.security.AccessToken

import cats.data.OptionT
import cats.effect.IO
import cats.effect.std.UUIDGen
import doobie._
import doobie.implicits._

trait TokenRepo[F[_]] {
  def createToken(user: User): F[AccessToken]

  def getUserByToken(token: AccessToken): OptionT[F, User]
}

object TokenRepo {
  private class Impl(xa: Transactor[IO]) extends TokenRepo[IO] {
    def insert(userId: Long, accessToken: String): IO[Unit] =
      sql"INSERT INTO access_token(value, user_id) VALUES ($accessToken, $userId)".update.run.transact(xa).void

    override def createToken(user: User): IO[AccessToken] = for {
      uuid <- UUIDGen[IO].randomUUID
      _ <- insert(user.id, uuid.toString)
    } yield AccessToken(uuid)

    def select(token: String): IO[Option[User]] =
      (sql"WITH user_ids AS(SELECT user_id FROM access_token WHERE value = $token) " ++
        sql"SELECT * FROM account WHERE id IN (SELECT * FROM user_ids)").query[User].option.transact(xa)

    override def getUserByToken(token: AccessToken): OptionT[IO, User] =
      OptionT(select(token.token.toString))
  }

  def make(xa: Transactor[IO]): TokenRepo[IO] = new Impl(xa)
}