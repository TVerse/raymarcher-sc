package eu.timevers.raymarcher

import cats.Applicative
import cats.syntax.apply.catsSyntaxApply
import cats.syntax.functor.toFunctorOps
import cats.syntax.traverse.toTraverseOps
import eu.timevers.raymarcher.Logger
import eu.timevers.raymarcher.primitives.*
import eu.timevers.raymarcher.scene.SDF

import scala.annotation.tailrec
import scala.collection.immutable.LazyList

class Raymarcher[F[_]](using S: Applicative[F]):
  def render(config: Config): F[Image] =
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
        config.renderSettings
      )
    }
    S.pure(Image(width, height, coloredPixels.map(RGBColor(_))))

  private def generatePixel(
      i: Int,
      j: Int,
      imageWidth: Int,
      imageHeight: Int,
      camera: Camera,
      renderSettings: RenderSettings
  ): Color =
    val u     = i.toDouble / (imageWidth - 1)
    val v     = j.toDouble / (imageHeight - 1)
    val r     = camera.getRay(u, v)
    val scene = SDF.sphere

    val unit            = r.direction.unit
    val t               = 0.5 * (unit.y + 1.0)
    val backgroundColor = (1 - t) * Color(1, 1, 1) + t * Color(0.5, 0.7, 1)

    @tailrec
    def step(depth: Double, count: Int): Color =
      val p    = r.origin + depth * r.direction
      val dist = scene.unSDF(p)
      if math.abs(dist) < renderSettings.epsilon then
        val normal = scene.estimateNormal(p)
        0.5 * (Color(1, 1, 1) + Color(normal.x, normal.y, normal.z))
      else if count > renderSettings.maxMarchingSteps then backgroundColor
      else step(depth + dist, count + 1)

    step(renderSettings.minDepth, 0)
