package eu.timevers.raymarcher.scene

import eu.timevers.raymarcher.primitives.{Color, Ray}

case class Scene(sdf: SDF, background: Background)
