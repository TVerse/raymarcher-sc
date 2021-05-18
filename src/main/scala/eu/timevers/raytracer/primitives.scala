package eu.timevers.raytracer

package primitives {

  import eu.timevers.raytracer.primitives.syntax._

  case class Vec3(x: Double, y: Double, z: Double) derives CanEqual

  object Vec3:
    def apply(uv: UnitVec3): Vec3 = Vec3(uv.x, uv.y, uv.z)

  case class UnitVec3(x: Double, y: Double, z: Double) derives CanEqual

  object UnitVec3:
    def apply(v: Vec3): UnitVec3 =
      val normVec = v / v.length
      UnitVec3(normVec.x, normVec.y, normVec.z)

  case class Point3(x: Double, y: Double, z: Double) derives CanEqual

  object Point3:
    val Origin: Point3 = Point3(0, 0, 0)

  case class Ray(origin: Point3, direction: Vec3) derives CanEqual

  case class Color(r: Double, g: Double, b: Double)

  case class RGBColor(r: Int, g: Int, b: Int)

  object RGBColor:
    def apply(r: Int, g: Int, b: Int): RGBColor =
      new RGBColor(clampToRange0To255(r), clampToRange0To255(g), clampToRange0To255(b))

    def apply(c: Color): RGBColor =
      RGBColor((c.r * 256).toInt, (c.g * 256).toInt, (c.b * 256).toInt)

    private def clampToRange0To255(i: Int): Int =
      if i < 0 then 0
      else if i > 255 then 255
      else i


  object syntax:
    extension (v: Vec3)
      inline def unary_- : Vec3 = -1 * v

      inline def +(other: Vec3): Vec3 = Vec3(v.x + other.x, v.y + other.y, v.z + other.z)

      inline def -(other: Vec3): Vec3 = v + (-other)

      inline def *(d: Double): Vec3 = Vec3(d * v.x, d * v.y, d * v.z)

      inline def /(d: Double): Vec3 = v * (1 / d)

      inline def norm: Double = v.x * v.x + v.y * v.y + v.z * v.z

      inline def length: Double = math.sqrt(norm)

      def unit: UnitVec3 = UnitVec3(v)

    extension (d: Double)
      inline def *(v: Vec3): Vec3 = v * d

      inline def *(c: Color): Color = Color(c.r * d, c.g * d, c.b * d)

    extension (p: Point3)
      def asVec: Vec3 = Vec3(p.x, p.y, p.z)

      inline def unary_- : Point3 = Point3(-p.x, -p.y, -p.z)

      inline def +(v: Vec3): Point3 = Point3(v.x + p.x, v.y + p.y, v.z + p.z)

      inline def -(v: Vec3): Point3 = p + (-v)

    extension (r: Ray)
      def at(t: Double): Point3 = r.origin + t * r.direction

    extension (c: Color)
      inline def +(other: Color): Color = Color(c.r + other.r, c.g + other.g, c.b + other.b)

}