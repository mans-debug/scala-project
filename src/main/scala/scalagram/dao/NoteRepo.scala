package org.itis.mansur
package scalagram.dao

import doobie.ConnectionIO
import doobie.implicits._
import org.itis.mansur.scalagram.models.Note

trait NoteRepo[F[_]] {
  def save(note: Note): F[Int]

  def update(note: Note): F[Note]

  def getById(noteId: Long): F[Option[Note]]

  def getByNotebookId(notebookId: Long): F[List[Note]]

  def delete(noteId: Long): F[Int]
}

object NoteRepo {
  object sql {
    def insert(title: String, notebookId: Long, content: String): doobie.Update0 =
      sql"INSERT INTO note(title, notebook_id, note_content) VALUES($title, $notebookId, $content)".update

    def update(id: Long, title: String, content: String): doobie.Update0 =
      sql"UPDATE note SET title = $title, note_content = $content WHERE id=$id".update

    def selectById(noteId: Long): doobie.Query0[Note] =
      sql"SELECT * FROM note WHERE id=$noteId".query[Note]

    def selectByNotebookId(notebookId: Long): doobie.Query0[Note] =
      sql"SELECT * FROM note WHERE notebook_id=$notebookId".query[Note]

    def deleteById(noteId: Long): doobie.Update0 =
      sql"DELETE FROM note WHERE id=$noteId".update
  }

  private class Impl extends NoteRepo[ConnectionIO] {
    override def save(note: Note): ConnectionIO[Int] =
      sql.insert(note.title, note.notebookId, note.content).run

    override def update(note: Note): ConnectionIO[Note] =
      sql.update(note.id, note.title, note.content).withUniqueGeneratedKeys[Note]()

    override def getById(noteId: Long): ConnectionIO[Option[Note]] =
      sql.selectById(noteId).option

    override def getByNotebookId(notebookId: Long): ConnectionIO[List[Note]] =
      sql.selectByNotebookId(notebookId).to[List]

    override def delete(noteId: Long): doobie.ConnectionIO[Int] =
      sql.deleteById(noteId).run
  }

  def make: NoteRepo[ConnectionIO] = new Impl
}
