package eu.timevers.raymarcher.scene

import cats.data.Kleisli
import cats.{implicits, Id}
import eu.timevers.raymarcher.primitives.{Color, Point3, UnitVec3, Vec3}

type Distance = Double

private type SDF = Point3 => Distance

private type MF = Point3 => Material

enum SceneMap:
  case Primitive(sdf: SDF, mf: MF)
  case WithMaterial(a: SceneMap, m: MF)
  case Union(a: SceneMap, b: SceneMap)
  case Intersect(a: SceneMap, b: SceneMap)
  case Subtract(a: SceneMap, b: SceneMap)
  case Translate(a: SceneMap, v: Vec3)
  case LinearBlend(a: SceneMap, b: SceneMap, k: Double)
  case ExponentialBlend(a: SceneMap, b: SceneMap, k: Double)
  case ScaleUniform(a: SceneMap, k: Double)

object SceneMap:
  val Epsilon                                     = 0.00001
  val defaultMaterialFunction: Point3 => Material = _ => Material.Default

  val const: SceneMap = Primitive(_ => 1, defaultMaterialFunction)

  object halfSpace:
    val negX: SceneMap = Primitive(p => p.x, defaultMaterialFunction)
    val posX: SceneMap = Primitive(p => -p.x, defaultMaterialFunction)
    val negY: SceneMap = Primitive(p => p.y, defaultMaterialFunction)
    val posY: SceneMap = Primitive(p => -p.y, defaultMaterialFunction)
    val negZ: SceneMap = Primitive(p => p.z, defaultMaterialFunction)
    val posZ: SceneMap = Primitive(p => -p.z, defaultMaterialFunction)

  val unitSphere: SceneMap =
    Primitive(p => p.asVec.length - 1, defaultMaterialFunction)

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
    def distance(p: Point3): Distance                        =
      sceneMap match
        case Primitive(sdf, _)         => sdf(p)
        case WithMaterial(a, _)        => a.distance(p)
        case Union(a, b)               =>
          val da = a.distance(p)
          val db = b.distance(p)
          if da <= db then da else db
        case Intersect(a, b)           =>
          val da = a.distance(p)
          val db = b.distance(p)
          if da > db then da else db
        case Subtract(a, b)            =>
          val da = a.distance(p)
          val db = b.distance(p)
          if da > -db then da else db
        case Translate(a, v)           => a.distance(p - v)
        case LinearBlend(a, b, k)      =>
          (1 - k) * a.distance(p) + k * b.distance(p)
        case ExponentialBlend(a, b, k) =>
          val res = math.exp(-k * a.distance(p)) + math.exp(-k * b.distance(p))
          -math.log(math.max(Epsilon, res)) / k
        case ScaleUniform(a, k)        => a.distance((p.asVec / k).asPoint) * k

    def material(p: Point3): Material                        =
      sceneMap match
        case Primitive(_, mf)     => mf(p)
        case WithMaterial(_, mf)  => mf(p)
        case Union(a, b)          =>
          val da = a.distance(p)
          val db = b.distance(p)
          if da <= db then a.material(p) else b.material(p)
        case Intersect(a, b)      =>
          val da = a.distance(p)
          val db = b.distance(p)
          if da > db then a.material(p) else b.material(p)
        case Subtract(a, b)       =>
          val da = a.distance(p)
          val db = b.distance(p)
          if da > -db then a.material(p) else b.material(p)
        case Translate(a, v)      => a.material(p - v)
        case LinearBlend(a, b, k) => a.material(p).linearBlend(b.material(p), k)
        case ExponentialBlend(a, b, k) =>
          a.material(p).exponentialBlend(b.material(p), k)
        case ScaleUniform(a, k)        => a.material((p.asVec / k).asPoint)

    inline def withMaterial(m: Point3 => Material): SceneMap =
      WithMaterial(sceneMap, m)

    inline def union(other: SceneMap): SceneMap = Union(sceneMap, other)

    inline def intersect(other: SceneMap): SceneMap = Intersect(sceneMap, other)

    inline def subtract(other: SceneMap): SceneMap = Subtract(sceneMap, other)

    inline def translate(v: Vec3): SceneMap = Translate(sceneMap, v)

    inline def linearBlend(other: SceneMap, k: Double): SceneMap =
      LinearBlend(sceneMap, other, k)

    inline def exponentialBlend(other: SceneMap, k: Double): SceneMap =
      ExponentialBlend(sceneMap, other, k)

    inline def scaleUniform(k: Double): SceneMap = ScaleUniform(sceneMap, k)

    def estimateNormal(p: Point3): UnitVec3 =
      val sdf: Point3 => Distance = sceneMap.distance
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
