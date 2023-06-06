package org.itis.mansur
package scalagram.dao

import doobie.implicits._
import doobie.{ConnectionIO, Query0, Update0}
import org.itis.mansur.scalagram.models.{Notebook, User}


trait NotebookRepo[F[_]] {
  def save(notebook: Notebook, user: User): F[Notebook]

  def update(notebook: Notebook): F[Notebook]

  def getById(noteId: Long): F[Option[Notebook]]

  def delete(notebookId: Long): F[Int]

  def getUserNotebooks(userId: Long): F[List[Notebook]]
}

object NotebookRepo {
  object sql {
    def selectByOwnerId(ownerId: Long): Query0[Notebook] =
      sql"SELECT * FROM notebook WHERE book_owner=$ownerId".query[Notebook]

    def selectById(notebookId: Long): Query0[Notebook] =
      sql"SELECT * FROM notebook WHERE id=$notebookId".query[Notebook]

    def insert(name: String, ownerId: Long): Update0 =
      sql"INSERT INTO notebook (book_name, book_owner) VALUES($name, $ownerId)".update

    def update(id: Long, name: String): Update0 =
      sql"UPDATE notebook SET book_name = $name WHERE id=$id".update

    def deleteById(notebookId: Long): Update0 =
      sql"DELETE FROM notebook WHERE id=$notebookId".update
  }

  private class Impl extends NotebookRepo[ConnectionIO] {
    override def save(notebook: Notebook, user: User): ConnectionIO[Notebook] =
      sql.insert(notebook.name, user.id).withUniqueGeneratedKeys[Notebook]("id")

    override def update(notebook: Notebook): ConnectionIO[Notebook] =
      sql.update(notebook.id, notebook.name).withUniqueGeneratedKeys[Notebook]()

    override def getById(notebookId: Long): ConnectionIO[Option[Notebook]] =
      sql.selectById(notebookId).option

    override def delete(notebookId: Long): ConnectionIO[Int] =
      sql.deleteById(notebookId).run

    override def getUserNotebooks(userId: Long): ConnectionIO[List[Notebook]] =
      sql.selectByOwnerId(userId).to[List]
  }

  def make: NotebookRepo[ConnectionIO] = new Impl
}

