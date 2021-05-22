package eu.timevers.raymarcher.image

case class Image[F[_]](
    width: Int,
    height: Int,
    pixels: fs2.Stream[F, RGBColor]
)
