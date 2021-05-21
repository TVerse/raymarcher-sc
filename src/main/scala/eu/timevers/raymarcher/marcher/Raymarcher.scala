package eu.timevers.raymarcher.marcher

import cats.Applicative
import cats.syntax.apply.catsSyntaxApply
import cats.syntax.functor.toFunctorOps
import cats.syntax.traverse.toTraverseOps
import eu.timevers.raymarcher.scene.{Camera, Material, Scene, SceneMap}
import eu.timevers.raymarcher.image.RGBColor
import eu.timevers.raymarcher.image.Image
import eu.timevers.raymarcher.primitives.Color
import eu.timevers.raymarcher.{
  Config,
  ImageSettings,
  MaterialOverride,
  RenderSettings
}
import eu.timevers.raymarcher.image.Image

import scala.annotation.tailrec
import scala.collection.immutable.LazyList

class Raymarcher[F[_]: Applicative]:
  def render(config: Config, scene: Scene): F[Image] =
    val height = config.imageSettings.height
    val width  = config.imageSettings.width
    val pixels = for
      j <- (height - 1) to 0 by -1
      i <- 0 until width
    yield (i, j)
    pixels.toList
      .traverse { case (i, j) =>
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
      .map(cps => Image(width, height, cps.map(RGBColor(_))))

  private def generatePixel(
      i: Int,
      j: Int,
      imageWidth: Int,
      imageHeight: Int,
      camera: Camera,
      renderSettings: RenderSettings,
      scene: Scene
  ): F[Color] =
    val u = i.toDouble / (imageWidth - 1)
    val v = j.toDouble / (imageHeight - 1)
    camera.getRay[F](u, v).map { r =>

      val sceneMap = scene.sceneMap

      @tailrec
      def step(depth: Double, count: Int): Color =
        val p        = r.origin + depth * r.direction
        val dist     = sceneMap.distance(p)
        val newDepth = dist + depth
        if math.abs(dist) < renderSettings.epsilon then
          renderSettings.materialOverride match
            case Some(MaterialOverride.Normal)     =>
              val normal = sceneMap.estimateNormal(p)
              0.5 * (Color(1, 1, 1) + Color(normal.x, normal.y, normal.z))
            case Some(MaterialOverride.Iterations) =>
              val frac = count.toDouble / renderSettings.maxMarchingSteps
              Color(frac, frac, frac)
            case None                              =>
              sceneMap.material(p) match
                case Material.Constant(c) => c
        else if count > renderSettings.maxMarchingSteps || newDepth > renderSettings.tMax then
          if renderSettings.materialOverride.contains(
              MaterialOverride.Iterations
            )
          then
            val frac = count.toDouble / renderSettings.maxMarchingSteps
            if frac > 0.99 then Color(1, 0, 1)
            else Color(frac, frac, frac)
          else scene.background(r)
        else step(newDepth, count + 1)

      step(renderSettings.tMin, 0)
    }
