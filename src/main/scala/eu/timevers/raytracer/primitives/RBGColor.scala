package eu.timevers.raytracer.primitives

case class RGBColor(r: Int, g: Int, b: Int)

object RGBColor:
  def apply(r: Int, g: Int, b: Int): RGBColor =
    new RGBColor(
      clampToRange0To255(r),
      clampToRange0To255(g),
      clampToRange0To255(b)
    )

  def apply(c: Color): RGBColor =
    RGBColor((c.r * 256).toInt, (c.g * 256).toInt, (c.b * 256).toInt)

  private def clampToRange0To255(i: Int): Int =
    if i < 0 then 0
    else if i > 255 then 255
    else i
