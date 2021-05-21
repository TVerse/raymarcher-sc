package eu.timevers.raymarcher.scene

import cats.Applicative
import cats.implicits.catsSyntaxApplicativeId
import eu.timevers.raymarcher.primitives.{Point3, Ray, Vec3}

case class Camera(
    origin: Point3,
    lowerLeftCorner: Point3,
    horizontal: Vec3,
    vertical: Vec3
)

object Camera:
  def apply(
      lookAt: Point3,
      lookFrom: Point3,
      up: Vec3,
      vfov: Double,
      aspectRatio: Double
  ): Camera =
    val theta          = vfov.toRadians
    val h              = math.tan(theta / 2)
    val viewportHeight = 2 * h
    val viewportWidth  = aspectRatio * viewportHeight

    val w = (lookFrom.asVec - lookAt.asVec).unit.asVec
    val u = up.cross(w).unit.asVec
    val v = w.cross(u).unit.asVec

    val horizontal = viewportWidth * u
    val vertical   = viewportHeight * v
    Camera(
      origin = lookFrom,
      horizontal = horizontal,
      vertical = vertical,
      lowerLeftCorner = lookFrom - horizontal / 2 - vertical / 2 - w
    )

  extension (c: Camera)
    def getRay[F[_]: Applicative](s: Double, t: Double): F[Ray] = Ray(
      c.origin,
      c.lowerLeftCorner.asVec + s * c.horizontal + t * c.vertical - c.origin.asVec
    ).pure
