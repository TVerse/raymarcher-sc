package eu.timevers.raymarcher.primitives

case class Vec3(x: Double, y: Double, z: Double) derives CanEqual

object Vec3:
  extension (v: Vec3)
    def asPoint: Point3 = Point3(v.x, v.y, v.z)

    inline def unary_- : Vec3 = -1 * v

    inline def +(other: Vec3): Vec3 =
      Vec3(v.x + other.x, v.y + other.y, v.z + other.z)

    inline def -(other: Vec3): Vec3 = v + (-other)

    inline def *(d: Double): Vec3 = d * v

    inline def /(d: Double): Vec3 = v * (1 / d)

    def dot(other: Vec3): Double = v.x * other.x + v.y * other.y + v.z * other.z

    def cross(other: Vec3): Vec3 = Vec3(
      x = v.y * other.z - v.z * other.y,
      y = v.z * other.x - v.x * other.z,
      z = v.x * other.y - v.y * other.x
    )

    inline def lengthSquared: Double = v.dot(v)

    inline def length: Double = math.sqrt(lengthSquared)

    inline def unit: UnitVec3 = UnitVec3(v)

    def maxComponent: Double = math.max(v.x, math.max(v.y, v.z))

  extension (d: Double)
    inline def *(v: Vec3): Vec3 = Vec3(d * v.x, d * v.y, d * v.z)
