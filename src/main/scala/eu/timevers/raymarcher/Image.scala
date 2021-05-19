package eu.timevers.raymarcher

import eu.timevers.raymarcher.primitives.RGBColor

case class Image(width: Int, height: Int, pixels: List[RGBColor])

extension (image: Image)
  def asPPM: String =
    val header      = s"P3\n${image.width} ${image.height}\n255\n"
    val imageString = image.pixels.map(c => s"${c.r} ${c.g} ${c.b}\n").mkString
    header + imageString
