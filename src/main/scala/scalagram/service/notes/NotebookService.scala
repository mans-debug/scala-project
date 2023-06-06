package org.itis.mansur
package scalagram.service.notes

import scalagram.dao.{NoteRepo, NotebookRepo, RoleRepo}
import scalagram.models._

import cats.Applicative
import cats.effect.IO
import doobie._
import doobie.implicits._
import org.itis.mansur.scalagram.models.security.AccessError
import tofu.Raise

trait NotebookService[F[_]] {
  def create(user: User, title: String): F[Notebook]

  def edit(user: User, notebookId: Long, newTitle: String): F[Notebook]

  def delete(user: User, notebookId: Long): F[Int]

  def addNote(user: User, note: Note): F[Int]

  def editNote(user: User, note: Note): F[Note]

  def deleteNote(user: User, note: Note): F[Int]

  def getUserNotebooks(user: User): F[List[Notebook]]

  def getNotebookNotes(user: User, notebookId: Long): F[List[Note]]

  def addRole(user: User, anotherUser: User, notebookId: Long, role: Role): F[Int]

  def removeRole(user: User, anotherUser: User, notebookId: Long): F[Int]

  def changeRole(user: User, anotherUser: User, notebookId: Long, role: Role): F[Int]
}

object NotebookService {
  class Impl(noteRepo: NoteRepo[ConnectionIO],
             notebookRepo: NotebookRepo[ConnectionIO],
             roleRepo: RoleRepo[ConnectionIO],
             val xa: Transactor[IO]) extends NotebookService[IO] {

    override def create(user: User, title: String): IO[Notebook] =
      notebookRepo.save(Notebook(0, title, user.id), user).transact[IO](xa)

    override def edit(user: User, notebookId: Long, newTitle: String): IO[Notebook] =
      checkAccess(user, notebookId, writeAccess = true) >>
        notebookRepo.update(Notebook(notebookId, newTitle, 0)).transact(xa)

    override def delete(user: User, notebookId: Long): IO[Int] =
      checkAccess(user, notebookId, writeAccess = true) >>
        notebookRepo.delete(notebookId).transact(xa)

    override def addNote(user: User, note: Note): IO[Int] =
      checkAccess(user, note.notebookId, writeAccess = true) >>
        noteRepo.save(note).transact(xa)

    override def editNote(user: User, note: Note): IO[Note] =
      checkAccess(user, note.notebookId, writeAccess = true) >>
        noteRepo.update(note).transact(xa)

    override def deleteNote(user: User, note: Note): IO[Int] =
      checkAccess(user, note.notebookId, writeAccess = true) >>
        noteRepo.delete(note.id).transact(xa)

    override def getUserNotebooks(user: User): IO[List[Notebook]] =
      notebookRepo.getUserNotebooks(user.id).transact(xa)

    override def getNotebookNotes(user: User, notebookId: Long): IO[List[Note]] =
      checkAccess(user, notebookId, writeAccess = false) >>
        noteRepo.getByNotebookId(notebookId).transact(xa)

    override def addRole(user: User, anotherUser: User, notebookId: Long, role: Role): IO[Int] =
      checkAccess(user, notebookId, writeAccess = true) >>
        roleRepo.addRole(user.id, notebookId, role).transact(xa)

    override def removeRole(user: User, anotherUser: User, notebookId: Long): IO[Int] =
      checkAccess(user, notebookId, writeAccess = true) >>
        roleRepo.removeRole(user.id, notebookId).transact(xa)

    override def changeRole(user: User, anotherUser: User, notebookId: Long, role: Role): IO[Int] =
      removeRole(user, anotherUser, notebookId) >>
        roleRepo.addRole(user.id, notebookId, role).transact(xa)

    private def checkAccess(user: User, notebookId: Long, writeAccess: Boolean): IO[Boolean] =
      roleRepo.getRole(user.id, notebookId).map {
        case None => false
        case Some(value) if value == Reader && writeAccess => false
        case _ => true
      }.transact(xa)
        .flatMap { value =>
          isOwner(user, notebookId).map(if (_) true else value)
        }.flatMap {
        case true => Applicative[IO].pure(true)
        case false => Raise[IO, AccessError].raise(new AccessError)
      }

    private def isOwner(user: User, notebookId: Long): IO[Boolean] = {
      notebookRepo.getById(notebookId).map {
        case Some(value) => value.ownerId == user.id
        case None => false
      }.transact(xa)
    }

  }

  def make(noteRepo: NoteRepo[ConnectionIO],
           notebookRepo: NotebookRepo[ConnectionIO],
           roleRepo: RoleRepo[ConnectionIO],
           xa: Transactor[IO]
          ): NotebookService[IO] = new Impl(noteRepo, notebookRepo, roleRepo, xa)
}
