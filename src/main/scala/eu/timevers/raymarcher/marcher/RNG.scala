package eu.timevers.raymarcher.marcher

import scala.util.Random
import cats.data.State
import cats.effect.Sync
import cats.implicits.*

class RNG[F[_]: Sync](seed: Long):
  private val rng: Random = new Random(seed)

  val nextInt: F[Int] = Sync[F].delay(rng.nextInt())

  val nextDouble: F[Double] = Sync[F].delay(rng.nextDouble())
