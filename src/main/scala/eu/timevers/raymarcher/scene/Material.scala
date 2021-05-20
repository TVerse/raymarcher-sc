package eu.timevers.raymarcher.scene

import eu.timevers.raymarcher.primitives.{Color}

/*
Material depends on point in 3D space
Only applied when SDF(p) = 0 (handle volumetrics later?)
So base form is Point3 => Material (refraction/reflection requires handling in marcher impl)
But blending could also use a measure of overlap (so SDF(p)?)
 */

enum Material:
  case Constant(c: Color)

object Material:
  val Default: Material = Constant(Color(1, 0, 1))

  extension (m: Material)
    def linearBlend(other: Material, k: Double): Material = (m, other) match
      case (Constant(a), Constant(b)) => Constant((1 - k) * a + k * b)
      case (first, second)            => second

    def exponentialBlend(other: Material, k: Double): Material =
      (m, other) match
        case (Constant(a), Constant(b)) => Constant(a)
        case (first, second)            => first
