package eu.timevers.raytracer

import eu.timevers.raytracer.primitives.{Point3, Vec3}
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global

import java.nio.file.Path
import scala.concurrent.{Await, duration}

val ImageFilePath = Path.of("image.ppm").nn

val C = Components[Task]

@main def main(): Unit =
  val aspectRatio = 16.0 / 9.0
  val imageWidth = 400
  val imageHeight = (imageWidth / aspectRatio).toInt
  val viewportHeight = 2.0
  val viewportWidth = viewportHeight * aspectRatio
  val config = Config(
    imageSettings = ImageSettings(
      width = imageWidth,
      height = imageHeight
    ),
    cameraSettings = CameraSettings(
      viewportHeight = viewportHeight,
      viewportWidth = viewportWidth,
      focalLength = 1.0,
      origin = Point3.Origin,
      horizontal = Vec3(viewportWidth, 0, 0),
      vertical = Vec3(0, viewportHeight, 0)
    )
  )
  val task = for
    renderResult <- C.raytracer.render(config)
    _ <- C.fileWriter.write(ImageFilePath)(renderResult)
  yield ()
  Await.result(task.runToFuture, 3.seconds)
