package com.seedtag.infra.client

import io.circe.*

case class IonCannon(generation: Int, available: Boolean) derives Codec.AsObject

case class DamageInflicted(casualties: Int, generation: Int) derives Codec.AsObject

case class FireCommand(target: Coordinates, enemies: Int) derives Codec.AsObject

case class Coordinates(
    x: Int,
    y: Int
) derives Codec.AsObject
