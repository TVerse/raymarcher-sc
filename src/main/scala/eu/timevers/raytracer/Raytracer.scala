package eu.timevers.raytracer

import cats.Applicative
import cats.syntax.apply.catsSyntaxApply
import cats.syntax.functor.toFunctorOps
import cats.syntax.traverse.toTraverseOps
import eu.timevers.raytracer.Logger
import eu.timevers.raytracer.primitives.{Color, Point3, RGBColor, Ray}

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
    val u = i.toDouble / (imageWidth - 1)
    val v = j.toDouble / (imageHeight - 1)
    val r = Ray(
      cameraSettings.origin,
      u * cameraSettings.horizontal + v * cameraSettings.vertical + cameraSettings.lowerLeftCorner.asVec - cameraSettings.origin.asVec
    )
    if hitSphere(Point3(0, 0, -1), 0.5)(r) then Color(1, 0, 0)
    else
      val unit = r.direction.unit
      val t    = 0.5 * (unit.y + 1.0)
      (1 - t) * Color(1, 1, 1) + t * Color(0.5, 0.7, 1)

  private def hitSphere(center: Point3, radius: Double)(r: Ray): Boolean =
    val oc           = (r.origin - center.asVec).asVec
    val a            = r.direction.norm
    val b            = 2 * oc.dot(r.direction)
    val c            = oc.norm - radius * radius
    val discriminant = b * b - 4 * a * c
    discriminant > 0
