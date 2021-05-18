package eu.timevers.raytracer.primitives

case class Point3(x: Double, y: Double, z: Double) derives CanEqual

object Point3:
  val Origin: Point3 = Point3(0, 0, 0)

  extension (p: Point3)
    def asVec: Vec3 = Vec3(p.x, p.y, p.z)

    inline def unary_- : Point3 = Point3(-p.x, -p.y, -p.z)

    inline def +(v: Vec3): Point3 = Point3(v.x + p.x, v.y + p.y, v.z + p.z)

    inline def -(v: Vec3): Point3 = p + (-v)
