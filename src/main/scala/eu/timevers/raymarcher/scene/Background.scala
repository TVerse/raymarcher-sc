package eu.timevers.raymarcher.scene

import eu.timevers.raymarcher.primitives.{Color, Ray}

opaque type Background = Ray => Color

object Background:
  def gradient(mixFunc: Ray => Double, from: Color, to: Color): Background =
    r =>
      val t = mixFunc(r)
      (1 - t) * from + t * to

  def uniform(c: Color): Background = _ => c

  extension (bg: Background) def apply(r: Ray): Color = bg(r)
