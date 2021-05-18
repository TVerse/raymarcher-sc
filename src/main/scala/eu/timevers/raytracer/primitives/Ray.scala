package eu.timevers.raytracer.primitives

case class Ray(origin: Point3, direction: Vec3) derives CanEqual

object Ray:
  extension (r: Ray) def at(t: Double): Point3 = r.origin + t * r.direction
