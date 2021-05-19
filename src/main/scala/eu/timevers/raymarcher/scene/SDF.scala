package eu.timevers.raymarcher.scene

import eu.timevers.raymarcher.primitives.{Point3, UnitVec3, Vec3}

case class SDF(unSDF: Point3 => Double)

object SDF:
  val Epsilon = 0.00001

  val sphere: SDF = SDF(p => p.asVec.length - 1)

  extension (sdf: SDF)
    def estimateNormal(p: Point3): UnitVec3 =
      val v = Vec3(
        x = sdf.unSDF(Point3(p.x + Epsilon, p.y, p.z)) - sdf.unSDF(
          Point3(p.x - Epsilon, p.y, p.z)
        ),
        y = sdf.unSDF(Point3(p.x, p.y + Epsilon, p.z)) - sdf.unSDF(
          Point3(p.x, p.y - Epsilon, p.z)
        ),
        z = sdf.unSDF(Point3(p.x, p.y, p.z + Epsilon)) - sdf.unSDF(
          Point3(p.x, p.y, p.z - Epsilon)
        )
      )
      v.unit
