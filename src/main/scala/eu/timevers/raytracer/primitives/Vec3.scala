package eu.timevers.raytracer.primitives

case class Vec3(x: Double, y: Double, z: Double) derives CanEqual

object Vec3:
  def apply(uv: UnitVec3): Vec3 = Vec3(uv.x, uv.y, uv.z)

  extension (v: Vec3)
    inline def unary_- : Vec3 = -1 * v

    inline def +(other: Vec3): Vec3 =
      Vec3(v.x + other.x, v.y + other.y, v.z + other.z)

    inline def -(other: Vec3): Vec3 = v + (-other)

    inline def *(d: Double): Vec3 = d * v

    inline def /(d: Double): Vec3 = v * (1 / d)

    inline def norm: Double = v.x * v.x + v.y * v.y + v.z * v.z

    inline def length: Double = math.sqrt(norm)

    def unit: UnitVec3 = UnitVec3(v)

  extension (d: Double)
    inline def *(v: Vec3): Vec3 = Vec3(d * v.x, d * v.y, d * v.z)
