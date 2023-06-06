package org.itis.mansur
package scalagram.dao

import org.itis.mansur.scalagram.models.Role.{fromStr, strMapping}

import doobie.ConnectionIO
import org.itis.mansur.scalagram.models.Role

trait RoleRepo[F[_]] {
  def addRole(userId: Long, notebookId: Long, role: Role): F[Int]

  def getRole(userId: Long, notebookId: Long): F[Option[Role]]

  def removeRole(userId: Long, notebookId: Long): F[Int]
}

object RoleRepo {
  object sql {
    def insert(userId: Long, notebookId: Long, role: String): doobie.Update0 =
      sql"INSERT INTO roles (user_id, notebook_id, user_role) VALUES($userId, $notebookId, $role)".update

    def selectById(userId: Long, notebookId: Long): doobie.Query0[String] =
      sql"SELECT user_role FROM roles WHERE user_id=$userId AND notebook_id=$notebookId".query[String]

    def deleteById(userId: Long, notebookId: Long): doobie.Update0 =
      sql"DELETE FROM roles WHERE user_id=$userId AND notebook_id=$notebookId".update
  }

  private class Impl extends RoleRepo[ConnectionIO] {
    override def addRole(userId: Long, notebookId: Long, role: Role): doobie.ConnectionIO[Int] =
      sql.insert(userId, notebookId, strMapping(role)).run

    override def getRole(userId: Long, notebookId: Long): ConnectionIO[Option[Role]] =
      sql.selectById(userId, notebookId).map(fromStr).option

    override def removeRole(userId: Long, notebookId: Long): ConnectionIO[Int] =
      sql.deleteById(userId, notebookId).run
  }

  def make: RoleRepo[ConnectionIO] = new Impl
}
