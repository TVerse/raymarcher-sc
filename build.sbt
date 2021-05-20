val scala3Version = "3.0.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "raymarcher-sc",
    version := "0.1.0",
    scalaVersion := scala3Version,
    scalacOptions ++= Seq(
      "-Xfatal-warnings",
      "-Yexplicit-nulls",
//      "-Ysafe-init", // Inaccurate warning (https://github.com/lampepfl/dotty/issues/12544), fixed in 3.0.1
      "-deprecation",
      "-feature",
      "-unchecked"
    ),
    fork := true,
    javaOptions ++= Seq(
      "-XX:+UseG1GC",
      "-Xmx4G",
    ),
    libraryDependencies += "io.monix"      %% "monix"     % "3.4.0",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % "test"
  )
