package com.seedtag.domain

case class Report(
    target: Coordinates,
    casualties: Int,
    generation: Int
)

object Report:
  def apply(target: DroidVisionPoint, damageInflicted: DamageInflicted): Report =
    Report(target.coordinates, damageInflicted.casualties, damageInflicted.generation)
