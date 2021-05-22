package eu.timevers.raymarcher.primitives

import cats.Monoid

case class Color(r: Double, g: Double, b: Double)

object Color:
  extension (d: Double)
    inline def *(c: Color): Color = Color(c.r * d, c.g * d, c.b * d)

  extension (c: Color)
    inline def +(other: Color): Color =
      Color(c.r + other.r, c.g + other.g, c.b + other.b)

    inline def *(d: Double): Color = d * c

    inline def /(d: Double): Color = 1 / d * c

  given Monoid[Color] with
    def combine(x: Color, y: Color): Color =
      Color(x.r + y.r, x.g + y.g, x.b + y.b)

    val empty: Color = Color(0, 0, 0)
