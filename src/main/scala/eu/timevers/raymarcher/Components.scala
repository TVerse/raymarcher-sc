package eu.timevers.raymarcher

import cats.effect.Async
import cats.effect.Concurrent
import cats.effect.std.Console
import eu.timevers.raymarcher.file.FileWriter
import eu.timevers.raymarcher.marcher.Raymarcher

import scala.util.Random

class Components[F[_]: Async: Concurrent: Console]:
  val parallelism: Int =
    Runtime.getRuntime.availableProcessors() / 2 // TODO impure!

  val raymarcher: Raymarcher[F] = Raymarcher[F](parallelism)

  val fileWriter: FileWriter[F] = FileWriter[F]
