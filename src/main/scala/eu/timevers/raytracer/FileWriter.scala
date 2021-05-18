package eu.timevers.raytracer

import cats.effect.Sync
import cats.syntax.all.catsSyntaxApply
import monix.eval.Task

import java.io.BufferedWriter
import java.nio.file.{Files, Path}

class FileWriter[F[_]](using S: Sync[F]):
  def write(file: Path)(image: Image): F[Unit] =
    S.delay(Files.writeString(file, image.asPPM))
