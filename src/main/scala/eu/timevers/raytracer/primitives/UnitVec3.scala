package eu.timevers.raytracer.primitives

case class UnitVec3(x: Double, y: Double, z: Double) derives CanEqual

object UnitVec3:
  def apply(v: Vec3): UnitVec3 =
    val normVec = v / v.length
    UnitVec3(normVec.x, normVec.y, normVec.z)

  extension (uv: UnitVec3) def asVec: Vec3 = Vec3(uv.x, uv.y, uv.z)
