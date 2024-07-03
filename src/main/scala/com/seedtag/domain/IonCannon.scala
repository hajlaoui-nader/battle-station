package com.seedtag.domain

import scala.util.control.NoStackTrace

case class IonCannon(generation: Int, available: Boolean)

case class DamageInflicted(casualties: Int, generation: Int)

case class FireCommand(target: Coordinates, enemies: Int)

object FireCommand:
  def fromTarget(target: DroidVisionPoint): FireCommand =
    val command = FireCommand(target.coordinates, target.enemies.number)
    command

sealed trait IonCannonError                             extends NoStackTrace
case class IonCannonCommunicationError(message: String) extends IonCannonError
