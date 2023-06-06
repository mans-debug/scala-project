package org.itis.mansur
package scalagram.config

import cats.ApplicativeError
import cats.effect.Resource
import io.circe.config.parser


case class AppConfig(db: DbConfig, http: HttpConfig)

object AppConfig {
  def getConfig[F[_] : ApplicativeError[*[_], Throwable]]: Resource[F, AppConfig] = {
    Resource.eval(parser.decodeF[F, AppConfig]())
  }
}

case class HttpConfig(port: Int)

case class DbConfig(url: String,
                    user: String,
                    password: String,
                    migrationsPath: String,
                    driverClass: String,
                    poolConnections: Int)