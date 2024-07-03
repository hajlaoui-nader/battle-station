package com.seedtag.domain

import org.scalacheck.Gen

object generators:

  val genCoordinates: Gen[Coordinates] =
    for
      x <- Gen.posNum[Int]
      y <- Gen.posNum[Int]
    yield Coordinates(x, y)

  val genEnemyType: Gen[EnemyType] =
    Gen.oneOf(EnemyType.values.toList)

  val genEnemies: Gen[Enemies] =
    for
      enemyType <- genEnemyType
      number    <- Gen.posNum[Int]
    yield Enemies(enemyType, number)

  val genDroidVisionPoint: Gen[DroidVisionPoint] =
    for
      coordinates <- genCoordinates.retryUntil(Coordinates.distance(_, Coordinates(0, 0)) <= 100)
      enemies     <- genEnemies
      allies      <- Gen.option(Gen.posNum[Int])
    yield DroidVisionPoint(coordinates, enemies, allies)

  // genBigDistanceScan is a generator that creates a DroidRadarScan with a distance greater than 100 km
  val genBigDistanceScan: Gen[DroidRadarScan] =
    for
      protocols <- Gen.oneOf(Protocol.CLOSEST_ENEMIES, Protocol.FURTHEST_ENEMIES).map(List(_))
      scan <- Gen.listOfN(
        3,
        genDroidVisionPoint.retryUntil(p => Coordinates.distance(p.coordinates, Coordinates(0, 0)) <= 100)
      )
    yield DroidRadarScan(protocols, scan)

  private val allCompatibleProtocolsCombinations: List[List[Protocol]] =
    List(
      List(Protocol.CLOSEST_ENEMIES),
      List(Protocol.FURTHEST_ENEMIES),
      List(Protocol.ASSIST_ALLIES),
      List(Protocol.AVOID_CROSSFIRE),
      List(Protocol.PRIORITIZE_MECH),
      List(Protocol.AVOID_MECH),
      List(Protocol.CLOSEST_ENEMIES, Protocol.ASSIST_ALLIES),
      List(Protocol.CLOSEST_ENEMIES, Protocol.AVOID_CROSSFIRE),
      List(Protocol.CLOSEST_ENEMIES, Protocol.PRIORITIZE_MECH),
      List(Protocol.CLOSEST_ENEMIES, Protocol.AVOID_MECH),
      List(Protocol.FURTHEST_ENEMIES, Protocol.ASSIST_ALLIES),
      List(Protocol.FURTHEST_ENEMIES, Protocol.AVOID_CROSSFIRE),
      List(Protocol.FURTHEST_ENEMIES, Protocol.PRIORITIZE_MECH),
      List(Protocol.FURTHEST_ENEMIES, Protocol.AVOID_MECH)
    )

  private val allIncompatibleProtocolsCombinations: List[List[Protocol]] =
    List(
      List(Protocol.ASSIST_ALLIES, Protocol.AVOID_CROSSFIRE),
      List(Protocol.PRIORITIZE_MECH, Protocol.AVOID_MECH)
    )

  val genDroidRadarScan: Gen[DroidRadarScan] =
    for
      protocols <- Gen.oneOf(allCompatibleProtocolsCombinations)
      scan <-
        if protocols.contains(Protocol.ASSIST_ALLIES) then
          Gen.listOfN(3, genDroidVisionPoint).retryUntil(_.exists(_.allies.isDefined))
        else if protocols.contains(Protocol.PRIORITIZE_MECH) then
          Gen.listOfN(3, genDroidVisionPoint).retryUntil(_.exists(_.enemies.`type` == EnemyType.MECH))
        else if protocols.contains(Protocol.AVOID_MECH) then
          Gen.listOfN(3, genDroidVisionPoint).retryUntil(_.exists(_.enemies.`type` == EnemyType.SOLDIER))
        else if protocols.contains(Protocol.AVOID_CROSSFIRE) then
          Gen.listOfN(3, genDroidVisionPoint).retryUntil(_.exists(_.allies.isEmpty))
        else
          Gen.listOfN(3, genDroidVisionPoint)
    yield DroidRadarScan(protocols, scan)

  val genIncompatibleDroidRadarScan: Gen[DroidRadarScan] =
    for
      protocols <- Gen.oneOf(allIncompatibleProtocolsCombinations)
      scan      <- Gen.listOfN(3, genDroidVisionPoint)
    yield DroidRadarScan(protocols, scan)

  val genAvailableIonCannon: Gen[IonCannon] =
    for
      generation <- Gen.choose(1, 3)
      available  <- Gen.const(true)
    yield IonCannon(generation, available)

  val genFireCommand: Gen[FireCommand] =
    for
      coordinates <- genCoordinates
      enemies     <- Gen.posNum[Int]
    yield FireCommand(coordinates, enemies)

  val genDamageInflicted: Gen[DamageInflicted] =
    for
      casualties <- Gen.posNum[Int]
      generation <- Gen.posNum[Int]
    yield DamageInflicted(casualties, generation)

  val genReport: Gen[Report] =
    for
      coordinates <- genCoordinates
      casualties  <- Gen.posNum[Int]
      generation  <- Gen.posNum[Int]
    yield Report(coordinates, casualties, generation)
