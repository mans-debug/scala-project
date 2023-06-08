package org.itis.mansur
package scalagram

import cats.effect.{ExitCode, IO, IOApp}
import org.itis.mansur.scalagram.builders.ServerBuilder
import tofu.logging.Logging

object Server extends IOApp{

  implicit val makeLogging: Logging.Make[IO] = Logging.Make.plain[IO]

  override def run(args: List[String]): IO[ExitCode] = new ServerBuilder().buildServer.use(_ => IO.never).as(ExitCode.Success)
}
