package eu.timevers.raymarcher.scene

import eu.timevers.raymarcher.primitives.{Point3, UnitVec3, Vec3}

opaque type SDF = Point3 => Double

object SDF:
  val Epsilon = 0.00001

  val const: SDF = _ => 1

  object halfSpace:
    val negX: SDF  = _.x
    val posX: SDF = -_.x
    val negY: SDF  = _.y
    val posY: SDF = -_.y
    val negZ: SDF  = _.z
    val posZ: SDF = -_.z

  val unitSphere: SDF = _.asVec.length - 1

  // TODO inefficient
  val unitCube: SDF =
    halfSpace.negX.translate(Vec3(0.5, 0, 0))
      .intersect(halfSpace.posX.translate(Vec3(-0.5, 0, 0)))
      .intersect(halfSpace.negY.translate(Vec3(0, 0.5, 0)))
      .intersect(halfSpace.posY.translate(Vec3(0, -0.5, 0)))
      .intersect(halfSpace.negZ.translate(Vec3(0, 0, 0.5)))
      .intersect(halfSpace.posZ.translate(Vec3(0, 0, -0.5)))

  extension (sdf: SDF)
    def apply(p: Point3): Double = sdf(p)

    def union(other: SDF): SDF =
      p => math.min(sdf(p), other(p))

    def smoothUnion(other: SDF, k: Double): SDF = p =>
      val res = math.exp(-k * sdf(p)) + math.exp(-k * other(p))
      -math.log(math.max(Epsilon, res)) / k

    def intersect(other: SDF): SDF =
      (p => math.max(sdf(p), other(p)))

    def subtract(other: SDF): SDF =
      (p => math.max(sdf(p), -other(p)))

    def translate(v: Vec3): SDF = (p => sdf(p - v))

    def blend(other: SDF, a: Double): SDF =
      (p => a * sdf(p) + (1 - a) * other(p))

    def scaleUniform(f: Double): SDF = (p => sdf((p.asVec / f).asPoint) * f)

    def estimateNormal(p: Point3): UnitVec3 =
      val v = Vec3(
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
