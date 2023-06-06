package org.itis.mansur
package scalagram.models

import cats.implicits.catsSyntaxSemigroup
import tofu.logging.{DictLoggable, LogRenderer, Loggable}

case class User(id: Long, login: String, passwordHash: String)

object User {
  implicit val loggable: Loggable[User] =
    new DictLoggable[User] {
      def logShow(a: User): String = a.login
      def fields[I, V, R, S](a: User, i: I)(implicit
                                         r: LogRenderer[I, V, R, S]
      ): R =
        r.addString("id", a.id.toString, i) |+|
          r.addString("username", a.login, i)
    }
}
