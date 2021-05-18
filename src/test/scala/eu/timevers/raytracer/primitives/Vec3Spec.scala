package eu.timevers.raytracer.primitives

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class Vec3Spec extends AnyFlatSpec with Matchers:

  behavior of "Vec3"

  it should "negate" in {
    val v        = Vec3(1, 1, 1)
    val expected = Vec3(-1, -1, -1)
    -v should be(expected)
  }
