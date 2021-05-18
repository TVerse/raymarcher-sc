package eu.timevers.raytracer

import eu.timevers.raytracer.primitives.{Point3, Vec3}

case class Config(imageSettings: ImageSettings, cameraSettings: CameraSettings)

case class ImageSettings(width: Int, height: Int)

case class CameraSettings(
    viewportHeight: Double,
    viewportWidth: Double,
    focalLength: Double,
    origin: Point3,
    horizontal: Vec3,
    vertical: Vec3
):
  val lowerLeftCorner: Point3 =
    origin - horizontal / 2 - vertical / 2 - Vec3(0, 0, focalLength)
