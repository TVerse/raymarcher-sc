package eu.timevers.raymarcher

import eu.timevers.raymarcher.primitives.{Point3, Vec3, Color}
import eu.timevers.raymarcher.scene.{Background, SDF, Scene}
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global

import java.nio.file.Path
import scala.concurrent.Await
import scala.concurrent.duration.given

val ImageFilePath = Path.of("image.ppm").nn

val C = Components[Task]

@main def main(): Unit =
  val aspectRatio = 16.0 / 9.0
  val imageWidth  = 800
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
      vfov = 20,
      aspectRatio = aspectRatio
    ),
    renderSettings = RenderSettings(
      maxMarchingSteps = 1000,
      tMin = 0,
      tMax = 100
    )
  )
  val scene       = Scene(
    sdf = SDF.unitSphere
      .translate(Vec3(-0.5, 0.25, 0))
      .scaleUniform(1.125)
      .smoothUnion(SDF.unitSphere.translate(Vec3(0.5, 0, 0)), k = 32)
      .union(SDF.unitCube.translate(Vec3(0, -1.5, 0)))
      .subtract(SDF.halfSpace.posX.translate(Vec3(0, 0, 0))),
    background = Background.gradient(
      r => 0.5 * (r.direction.unit.y + 1.0),
      Color(1, 1, 1),
      Color(0, 0, 0)
    )
  )

  val task = for
    renderResult <- C.raymarcher.render(config, scene)
    _            <- C.fileWriter.write(ImageFilePath)(renderResult)
  yield ()
  Await.result(task.runToFuture, 10.seconds)
