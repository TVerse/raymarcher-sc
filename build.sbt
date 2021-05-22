val scala3Version = "3.0.0"

val catsEffectVersion = "3.1.1"
val fs2Version        = "3.0.4"

lazy val root = project
  .in(file("."))
  .settings(
    name := "raymarcher-sc",
    version := "0.1.0",
    scalaVersion := scala3Version,
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalacOptions ++= Seq(
//      "-Ysafe-init", // Inaccurate warning (https://github.com/lampepfl/dotty/issues/12544), fixed in 3.0.1
    ),
    fork := true,
    javaOptions ++= Seq(
      "-XX:+UseG1GC",
      "-Xmx8G"
    ),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      "co.fs2"        %% "fs2-core"    % fs2Version,
      "co.fs2"        %% "fs2-io"      % fs2Version
    ),
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % "test"
  )
