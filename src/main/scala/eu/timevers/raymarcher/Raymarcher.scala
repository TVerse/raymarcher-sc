package eu.timevers.raymarcher

import cats.Applicative
import cats.syntax.apply.catsSyntaxApply
import cats.syntax.functor.toFunctorOps
import cats.syntax.traverse.toTraverseOps
import eu.timevers.raymarcher.Logger
import eu.timevers.raymarcher.primitives.*
import eu.timevers.raymarcher.scene.{SDF, Scene}

import scala.annotation.tailrec
import scala.collection.immutable.LazyList

class Raymarcher[F[_]](using S: Applicative[F]):
  def render(config: Config, scene: Scene): F[Image] =
    val height        = config.imageSettings.height
    val width         = config.imageSettings.width
    val pixels        = for
      j <- (height - 1) to 0 by -1
      i <- 0 until width
    yield (i, j)
    val coloredPixels = pixels.toList.map { case (i, j) =>
      generatePixel(
        i,
        j,
        width,
        height,
        config.camera,
        config.renderSettings,
        scene
      )
    }
    S.pure(Image(width, height, coloredPixels.map(RGBColor(_))))

  private def generatePixel(
      i: Int,
      j: Int,
      imageWidth: Int,
      imageHeight: Int,
      camera: Camera,
      renderSettings: RenderSettings,
      scene: Scene
  ): Color =
    val u = i.toDouble / (imageWidth - 1)
    val v = j.toDouble / (imageHeight - 1)
    val r = camera.getRay(u, v)

    val backgroundColor = scene.background(r)

    val sdf = scene.sdf

    @tailrec
    def step(depth: Double, count: Int): Color =
      val p    = r.origin + depth * r.direction
      val dist = sdf(p)
      val newDepth = dist + depth
      if math.abs(dist) < renderSettings.epsilon then
        val normal = sdf.estimateNormal(p)
        0.5 * (Color(1, 1, 1) + Color(normal.x, normal.y, normal.z))
//        val frac   = count.toDouble / renderSettings.maxMarchingSteps
//        Color(frac, frac, frac)
      else
        if count > renderSettings.maxMarchingSteps || newDepth > renderSettings.tMax then
          backgroundColor
        else step(newDepth, count + 1)

    step(renderSettings.tMin, 0)
