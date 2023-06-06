package org.itis.mansur
package scalagram.dao

import scalagram.models.User

import cats.data.OptionT
import doobie.{ConnectionIO, Query0}
import doobie.util.query.Query
import doobie.util.transactor
import doobie.util.update.Update0

trait UserRepo[F[_]] {

  def save(user: User): F[User]

  def getById(id: Long): ConnectionIO[Option[User]]

  def getAll: F[List[User]]

  def getByLogin(login: String): ConnectionIO[Option[User]]

}

object UserRepo {

  import scalagram.models.User

  import cats.data.OptionT
  import doobie.implicits._

  object sql {
    def save(login: String, password: String): Update0 = sql"INSERT INTO account(login, password_hash) VALUES (${login}, ${password})".update
    def getById(id: Long): Query0[User] = sql"SELECT * FROM account WHERE id=$id".query[User]
    def getAll: Query0[User] = sql"SELECT * FROM account".query[User]
    def getByLogin(login: String): Query0[User] = sql"SELECT * FROM account WHERE login = $login".query[User]
  }

  private class Impl extends UserRepo[ConnectionIO] {
    override def save(user: User): ConnectionIO[User] = sql.save(user.login, user.passwordHash).withUniqueGeneratedKeys("id")
    override def getById(id: Long): ConnectionIO[Option[User]] = sql.getById(id).option
    override def getAll: ConnectionIO[List[User]] = sql.getAll.to[List]

    override def getByLogin(login: String): ConnectionIO[Option[User]] = sql.getByLogin(login).option
  }


  def make: UserRepo[ConnectionIO] = new Impl
}
