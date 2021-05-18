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
      "-Ysafe-init"
    ),

    libraryDependencies += "io.monix" %% "monix" % "3.4.0",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % "test",
  )
