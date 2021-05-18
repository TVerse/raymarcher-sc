package eu.timevers.raytracer

import cats.Applicative
import cats.syntax.apply.catsSyntaxApply
import cats.syntax.functor.toFunctorOps
import cats.syntax.traverse.toTraverseOps
import eu.timevers.raytracer.Logger
import eu.timevers.raytracer.primitives.*

class Raytracer[F[_]](using S: Applicative[F], L: Logger[F]):
  def render(config: Config): F[Image] =
    val height        = config.imageSettings.height
    val width         = config.imageSettings.width
    val pixels        = for
      j <- (height - 1) to 0 by -1
      i <- 0 until width
    yield (i, j)
    val coloredPixels = pixels.toList.traverse { case (i, j) =>
      val message =
        if i == 0 then L.info(s"Scanlines remaining: $j") else S.pure(())
      message *> S.pure(
        generatePixel(i, j, width, height, config.cameraSettings)
      )
    }
    coloredPixels.map(cs => Image(width, height, cs.map(RGBColor(_))))

  private def generatePixel(
      i: Int,
      j: Int,
      imageWidth: Int,
      imageHeight: Int,
      cameraSettings: CameraSettings
  ): Color =
    val u        = i.toDouble / (imageWidth - 1)
    val v        = j.toDouble / (imageHeight - 1)
    val uv: Vec3 = u * cameraSettings.horizontal
    val vv: Vec3 = v * cameraSettings.vertical
    val x: Vec3  = uv + vv + cameraSettings.lowerLeftCorner.asVec
    val r        = Ray(cameraSettings.origin, x - cameraSettings.origin.asVec)
    val unit     = r.direction.unit
    val t        = 0.5 * (unit.y + 1.0)
    Color(1 - t + 0.5 * t, 1 - t + 0.7 * t, 1)
    (1 - t) * Color(1, 1, 1) + t * Color(0.5, 0.7, 1)
