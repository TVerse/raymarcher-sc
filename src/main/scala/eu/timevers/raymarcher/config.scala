package eu.timevers.raymarcher

import eu.timevers.raymarcher.primitives.{Point3, Vec3}

case class Config(
    imageSettings: ImageSettings,
    camera: Camera,
    renderSettings: RenderSettings
)

case class ImageSettings(width: Int, height: Int)

case class RenderSettings(
    maxMarchingSteps: Int,
    tMin: Double,
    tMax: Double,
    epsilon: Double = 1e-5
)
