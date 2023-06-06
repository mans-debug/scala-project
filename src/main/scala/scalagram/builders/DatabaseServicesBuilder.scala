package org.itis.mansur
package scalagram.builders

import cats.effect.kernel.Async
import cats.effect.{Resource, Sync}
import cats.syntax.functor._
import doobie.ExecutionContexts
import doobie.hikari.HikariTransactor
import doobie.util.transactor.Transactor
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.Location
import org.itis.mansur.scalagram.config.DbConfig

object DatabaseServicesBuilder {

  def migrateDatabase[F[_] : Sync](databaseConfig: DbConfig): F[Unit] = {
    Sync[F].delay {
      Flyway.configure()
        .dataSource(databaseConfig.url, databaseConfig.user, databaseConfig.password)
        .locations(new Location(databaseConfig.migrationsPath))
        .baselineOnMigrate(true)
        .load()
        .migrate()
    }.as {
      ()
    }
  }

  def transactor[F[_] : Async](databaseConfig: DbConfig): Resource[F, Transactor[F]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[F](databaseConfig.poolConnections)
      transactor <- HikariTransactor.newHikariTransactor[F](
        databaseConfig.driverClass,
        databaseConfig.url,
        databaseConfig.user,
        databaseConfig.password,
        ce
      )
    } yield transactor


}
