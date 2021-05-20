package eu.timevers.raymarcher.scene

import eu.timevers.raymarcher.primitives.{Color}

enum Material:
  case Constant(c: Color)
  case Normal
  case Iterations

object Material:
  val Default: Material = Constant(Color(1, 0, 1))

  extension (m: Material)
    def linearBlend(other: Material, a: Double): Material = (m, other) match
      case (Constant(first), Constant(other)) => Constant(first)
      case (first, second)                    => first

    def exponentialBlend(other: Material, k: Double): Material =
      (m, other) match
        case (Constant(first), Constant(other)) => Constant(first)
        case (first, second)                    => first
