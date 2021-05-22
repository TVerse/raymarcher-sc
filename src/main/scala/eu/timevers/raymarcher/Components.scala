package eu.timevers.raymarcher

import cats.effect.Async
import cats.effect.Concurrent
import cats.effect.std.Console
import eu.timevers.raymarcher.file.FileWriter
import eu.timevers.raymarcher.marcher.{RNG, Raymarcher}

import scala.util.Random

class Components[F[_]: Async: Concurrent: Console]:
  val parallelism: Int =
    Runtime.getRuntime.availableProcessors() / 2 // TODO impure!

  val seed: Long =
    java.lang.Double.doubleToRawLongBits(math.E * math.pow(10, 10))

  val rng: RNG[F] = RNG[F](seed)

  val raymarcher: Raymarcher[F] = Raymarcher[F](rng, parallelism)

  val fileWriter: FileWriter[F] = FileWriter[F]
