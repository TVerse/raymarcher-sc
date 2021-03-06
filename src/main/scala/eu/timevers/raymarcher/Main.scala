package eu.timevers.raymarcher

import eu.timevers.raymarcher.primitives.{Color, Point3, Vec3}
import eu.timevers.raymarcher.scene.{
  Background,
  Camera,
  Material,
  Scene,
  SceneMap
}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.Monad.ops.toAllMonadOps

import java.nio.file.Path
import scala.concurrent.Await
import scala.concurrent.duration
import scala.util.Random
import util.chaining.scalaUtilChainingOps

val ImageFilePath = Path.of("image.ppm").nn

@main def main(): Unit =
  type F[A] = IO[A]
  val C           = Components[F]
  val aspectRatio = 16.0 / 9.0
  val imageWidth  = 400
  val imageHeight = (imageWidth / aspectRatio).toInt
  val config      = Config(
    imageSettings = ImageSettings(
      width = imageWidth,
      height = imageHeight
    ),
    camera = Camera(
      lookAt = Point3.Origin,
      lookFrom = Point3(3, 4, 5),
      up = Vec3(0, 1, 0),
      vfov = 30,
      aspectRatio = aspectRatio
    ),
    renderSettings = RenderSettings(
      maxMarchingSteps = 1000,
      tMin = 0,
      tMax = 100,
      samplesPerPixel = 25
//      materialOverride = Some(MaterialOverride.Normal)
    )
  )

  def clampedSin(d: Double): Double = math.sin(d)

  val clampedSinMaterial: Point3 => Material = p =>
    Material.Constant(
      Color(clampedSin(p.x * 2), clampedSin(p.y * 3), clampedSin(p.z * 4))
    )
  val sceneMap                               = SceneMap.unitSphere
    .withMaterial(clampedSinMaterial)
    .translate(Vec3(-0.5, 0.25, 0))
    .scaleUniform(1.125)
    .exponentialBlend(
      SceneMap.unitSphere
        .withMaterial(clampedSinMaterial)
        .translate(Vec3(0.5, 0, 0)),
      k = 32
    )
//    .exponentialBlend(SceneMap.unitSphere.withMaterial(clampedSinMaterial).scaleUniform(1.5).translate(Vec3(0, 1, -1)), k=32)
    .union(
      SceneMap.unitCube
        .translate(Vec3(0, -1.5, 0))
    )
//    .subtract(SceneMap.halfSpace.posX.translate(Vec3(0, 0, 0)))
    .union(
      SceneMap.halfSpace.negY
        .translate(Vec3(0, -2, 0))
        .withMaterial(clampedSinMaterial)
    )
  val scene                                  = Scene(
    sceneMap = sceneMap,
    background = Background.gradient(
      r => 0.5 * (r.direction.unit.y + 1.0),
      Color(1, 1, 1),
      Color(0, 0, 0)
    )
  )

  val task  = for
    renderResult <- C.raymarcher.render(config, scene)
    _ <- C.fileWriter.write(ImageFilePath)(renderResult).compile.drain
  yield ()
  val timed = task.timed.flatTap { case (time, _) =>
    Logger[F].info(s"Done! Took: ${time.toSeconds} seconds")
  }
  timed.unsafeRunSync()
