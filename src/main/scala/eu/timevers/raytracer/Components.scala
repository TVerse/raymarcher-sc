package eu.timevers.raytracer

import monix.eval.Task
import cats.effect.Sync

class Components[F[_]: Sync: Logger]:
  val fileWriter: FileWriter[F] = FileWriter[F]

  val raytracer: Raytracer[F] = Raytracer[F]
