package eu.timevers.raytracer

import eu.timevers.raytracer.primitives.{Point3, Vec3}
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global

import java.nio.file.Path
import scala.concurrent.Await
import scala.concurrent.duration.*

val ImageFilePath = Path.of("image.ppm").nn

val C = Components[Task]

@main def main(): Unit =
  val aspectRatio = 16.0 / 9.0
  val imageWidth  = 1200
  val imageHeight = (imageWidth / aspectRatio).toInt
  val config      = Config(
    imageSettings = ImageSettings(
      width = imageWidth,
      height = imageHeight
    ),
    camera = Camera(
      lookAt = Point3.Origin,
      lookFrom = Point3(0, 0, 5),
      up = Vec3(0, 1, 0),
      vfov = 45,
      aspectRatio = aspectRatio
    ),
    renderSettings = RenderSettings(maxMarchingSteps = 100)
  )
  val task        = for
    renderResult <- C.raymarcher.render(config)
    _            <- C.fileWriter.write(ImageFilePath)(renderResult)
  yield ()
  Await.result(task.runToFuture, 3.seconds)
