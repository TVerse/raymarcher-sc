package eu.timevers.raymarcher.marcher

import cats.Applicative
import cats.effect.Concurrent
import cats.implicits.given
import eu.timevers.raymarcher.scene.{Camera, Material, Scene, SceneMap}
import eu.timevers.raymarcher.image.RGBColor
import eu.timevers.raymarcher.image.Image
import eu.timevers.raymarcher.primitives.{Color, Ray}
import eu.timevers.raymarcher.{
  Config,
  ImageSettings,
  MaterialOverride,
  RenderSettings
}
import eu.timevers.raymarcher.image.Image
import eu.timevers.raymarcher.Logger

import scala.annotation.tailrec
import scala.collection.immutable.LazyList

class Raymarcher[F[_]: Applicative: Concurrent: Logger](
    rng: RNG[F],
    parallelism: Int
):
  def render(config: Config, scene: Scene): F[Image[F]] =
    val height = config.imageSettings.height
    val width  = config.imageSettings.width

    val heightIndices =
      fs2.Stream.unfoldLoop(height - 1)(j => (j, Option(j - 1).filter(_ > 0)))
    val widthIndices  =
      fs2.Stream.unfoldLoop(0)(i => (i, Option(i + 1).filter(_ < width)))

    val pixels = heightIndices
      .product(widthIndices)
      .parEvalMap(parallelism) { case (j, i) =>
        val log =
          if i == 0 && j % 100 == 0 then
            Logger[F].info(s"Remaining scanlines: $j")
          else Concurrent[F].unit
        log *> generatePixel(
          i,
          j,
          width,
          height,
          config.camera,
          config.renderSettings,
          scene
        )
      }

    Image[F](width, height, pixels.map(RGBColor(_))).pure

  private def generatePixel(
      i: Int,
      j: Int,
      imageWidth: Int,
      imageHeight: Int,
      camera: Camera,
      renderSettings: RenderSettings,
      scene: Scene
  ): F[Color] =
    val uv: F[(Double, Double)] = for
      du <- rng.nextDouble
      dv <- rng.nextDouble
      u = (i.toDouble + du) / (imageWidth - 1)
      v = (j.toDouble + dv) / (imageHeight - 1)
    yield (u, v)

    val uvs =
      fs2.Stream.eval(uv).repeat.take(renderSettings.samplesPerPixel).evalMap {
        case (u, v) =>
          camera.getRay[F](u, v).map { r =>
            step(r, renderSettings, scene)
          }
      }

    uvs.compile.foldMonoid.map(_ / renderSettings.samplesPerPixel)

  private def step(r: Ray, renderSettings: RenderSettings, scene: Scene) =
    val sceneMap = scene.sceneMap

    @tailrec
    def go(depth: Double, count: Int): Color =
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
      else go(newDepth, count + 1)

    go(renderSettings.tMin, 0)
