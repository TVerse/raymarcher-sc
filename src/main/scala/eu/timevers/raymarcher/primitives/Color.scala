package eu.timevers.raymarcher.primitives

case class Color(r: Double, g: Double, b: Double)

object Color:
  extension (d: Double)
    inline def *(c: Color): Color = Color(c.r * d, c.g * d, c.b * d)

  extension (c: Color)
    inline def +(other: Color): Color =
      Color(c.r + other.r, c.g + other.g, c.b + other.b)
