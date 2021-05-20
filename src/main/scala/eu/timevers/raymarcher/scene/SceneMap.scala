package eu.timevers.raymarcher.scene

import eu.timevers.raymarcher.primitives.{Color, Point3, UnitVec3, Vec3}
import cats.data.Kleisli
import cats.implicits.*
import cats.Id

type Distance = Double

// TODO split this function so the material/color isn't also constantly computed.
opaque type SceneMap = Point3 => (Distance, Material)

object SceneMap:
  val Epsilon = 0.00001

  val const: SceneMap = _ => (1, Material.Default)

  object halfSpace:
    val negX: SceneMap = p => (p.x, Material.Default)
    val posX: SceneMap = p => (-p.x, Material.Default)
    val negY: SceneMap = p => (p.y, Material.Default)
    val posY: SceneMap = p => (-p.y, Material.Default)
    val negZ: SceneMap = p => (p.z, Material.Default)
    val posZ: SceneMap = p => (-p.z, Material.Default)

  val unitSphere: SceneMap = p => (p.asVec.length - 1, Material.Default)

  // TODO inefficient
  val unitCube: SceneMap =
    halfSpace.negX
      .translate(Vec3(0.5, 0, 0))
      .intersect(halfSpace.posX.translate(Vec3(-0.5, 0, 0)))
      .intersect(halfSpace.negY.translate(Vec3(0, 0.5, 0)))
      .intersect(halfSpace.posY.translate(Vec3(0, -0.5, 0)))
      .intersect(halfSpace.negZ.translate(Vec3(0, 0, 0.5)))
      .intersect(halfSpace.posZ.translate(Vec3(0, 0, -0.5)))

  extension (sceneMap: SceneMap)
    def apply(p: Point3): (Distance, Material) = sceneMap(p)

    def withMaterial(m: Material): SceneMap = p => (sceneMap(p)(0), m)

    def union(other: SceneMap): SceneMap = p =>
      val first  = sceneMap(p)
      val second = other(p)
      if first(0) <= second(0) then first
      else second

    def intersect(other: SceneMap): SceneMap = p =>
      val first  = sceneMap(p)
      val second = other(p)
      if first(0) >= second(0) then first
      else second

    def subtract(other: SceneMap): SceneMap = p =>
      val first  = sceneMap(p)
      val second = other(p)
      if first(0) >= -second(0) then first
      else second

    def translate(v: Vec3): SceneMap = p => sceneMap(p - v)

    def blend(other: SceneMap, a: Double): SceneMap = p =>
      val first  = sceneMap(p)
      val second = other(p)
      ((1 - a) * first(0) + a * second(0), first(1).linearBlend(second(1), a))

    def exponentialBlend(other: SceneMap, k: Double): SceneMap = p =>
      val (firstDist, firstMat)   = sceneMap(p)
      val (secondDist, secondMat) = other(p)
      val res = math.exp(-k * firstDist) + math.exp(-k * secondDist)
      (
        -math.log(math.max(Epsilon, res)) / k,
        firstMat.exponentialBlend(secondMat, k)
      )

    def scaleUniform(f: Double): SceneMap = p =>
      val res = sceneMap((p.asVec / f).asPoint)
      (res(0) * f, res(1))

    def estimateNormal(p: Point3): UnitVec3 =
      val sdf: Point3 => Distance = sceneMap.map(_(0))
      val v                       = Vec3(
        x = sdf(Point3(p.x + Epsilon, p.y, p.z)) - sdf(
          Point3(p.x - Epsilon, p.y, p.z)
        ),
        y = sdf(Point3(p.x, p.y + Epsilon, p.z)) - sdf(
          Point3(p.x, p.y - Epsilon, p.z)
        ),
        z = sdf(Point3(p.x, p.y, p.z + Epsilon)) - sdf(
          Point3(p.x, p.y, p.z - Epsilon)
        )
      )
      v.unit
