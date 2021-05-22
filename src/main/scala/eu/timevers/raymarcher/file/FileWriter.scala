package eu.timevers.raymarcher.file

import cats.effect.Async
import cats.syntax.all.catsSyntaxApply
import eu.timevers.raymarcher.image.Image
import fs2.INothing

import java.io.BufferedWriter
import java.nio.file.{Files, Path, StandardOpenOption}

class FileWriter[F[_]](using S: Async[F]):
  extension (image: Image[F])
    def asPPM: fs2.Stream[F, Byte] =
      val header      =
        s"P3\n${image.width} ${image.height}\n255\n".getBytes("UTF-8").toList
      val imageString =
        image.pixels
          .map(c => s"${c.r} ${c.g} ${c.b}\n".getBytes("UTF-8"))
          .flatMap(fs2.Stream.emits)
      fs2.Stream(header).flatMap(fs2.Stream.emits) ++ imageString

  def write(file: Path)(image: Image[F]): fs2.Stream[F, INothing] =
    image.asPPM
      .through(
        fs2.io.file
          .Files[F]
          .writeAll(
            file,
            flags = Seq(
              StandardOpenOption.CREATE,
              StandardOpenOption.TRUNCATE_EXISTING
            )
          )
      )
