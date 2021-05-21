package eu.timevers.raymarcher

import cats.effect.Sync
import eu.timevers.raymarcher.file.FileWriter
import eu.timevers.raymarcher.marcher.Raymarcher

class Components[F[_]: Sync: Logger]:
  val fileWriter: FileWriter[F] = FileWriter[F]

  val raymarcher: Raymarcher[F] = Raymarcher[F]
