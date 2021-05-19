package eu.timevers.raymarcher

import monix.eval.Task
import cats.effect.Sync

class Components[F[_]: Sync: Logger]:
  val fileWriter: FileWriter[F] = FileWriter[F]

  val raymarcher: Raymarcher[F] = Raymarcher[F]
