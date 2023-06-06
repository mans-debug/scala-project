package org.itis.mansur
package scalagram.models

sealed trait Role

case object Reader extends Role

case object Writer extends Role

object Role {
  def strMapping(role: Role): String = role match {
    case Reader => "reader"
    case Writer => "writer"
  }

  def fromStr(role: String): Role = role match {
    case "reader" => Reader
    case "writer" => Writer
    case _ => throw new IllegalArgumentException
  }
}

