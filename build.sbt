ThisBuild / version := "0.1.0"

ThisBuild / scalaVersion := "2.13.11"

lazy val root = (project in file("."))
  .settings(
    name := "Scalagram",
    idePackagePrefix := Some("org.itis.mansur")
  )
val CatsVersion = "2.7.0"
val CirceVersion = "0.14.2"
val CirceConfigVersion = "0.8.0"
val DoobieVersion = "1.0.0-RC1"
val H2Version = "1.4.200"
val Http4sVersion = "0.23.12"
val KindProjectorVersion = "0.13.2"
val LogbackVersion = "1.2.11"
val Slf4jVersion = "1.7.36"
val FlywayVersion = "8.5.12"
val PostgresVersion = "42.4.0"
val BcryptVersion = "4.3.0"
val TofuVersion = "0.10.8"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % CatsVersion,
  "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
  "org.http4s" %% "http4s-circe" % Http4sVersion,
  "org.http4s" %% "http4s-dsl" % Http4sVersion,
  "io.circe" %% "circe-generic" % CirceVersion,
  "io.circe" %% "circe-literal" % CirceVersion,
  "io.circe" %% "circe-parser" % CirceVersion,
  "io.circe" %% "circe-config" % CirceConfigVersion,
  "ch.qos.logback" % "logback-classic" % LogbackVersion,
  "org.tpolecat" %% "doobie-core" % DoobieVersion,
  "org.tpolecat" %% "doobie-hikari"    % DoobieVersion,
  "org.tpolecat" %% "doobie-postgres"  % DoobieVersion,
  "org.postgresql" % "postgresql" % PostgresVersion,
  "org.flywaydb" % "flyway-core" % FlywayVersion,
  "com.github.t3hnar" %% "scala-bcrypt" % BcryptVersion,
  "tf.tofu" %% "tofu-core-ce3" % TofuVersion,
  "tf.tofu" %% "tofu-logging" % TofuVersion,
  "tf.tofu" %% "tofu-logging-derivation" % TofuVersion,
  "tf.tofu" %% "tofu-kernel-ce3-interop" % TofuVersion,
)

dependencyOverrides += "org.slf4j" % "slf4j-api" % Slf4jVersion


addCompilerPlugin(
  ("org.typelevel" %% "kind-projector" % KindProjectorVersion).cross(CrossVersion.full),
)
addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")


enablePlugins(JavaAppPackaging)

Compile / mainClass := Some("ru.itis.scalagram.Server")

dockerBaseImage := "openjdk"
dockerExposedPorts := Seq(8080)
Docker / packageName := "scalagram"
Docker / version := "0.0.1"
