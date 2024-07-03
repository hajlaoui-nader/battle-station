package com.seedtag.domain

import weaver.*
import weaver.scalacheck.Checkers

object DroidRadarScanSuite extends SimpleIOSuite with Checkers:

  implicit val showDroidRadarScan: cats.Show[DroidRadarScan] =
    cats.Show.fromToString

  pureTest("should find the closest enemy") {
    val droidRadarScan = DroidRadarScan(
      List.empty,
      List(
        DroidVisionPoint(Coordinates(0, 10), Enemies(EnemyType.MECH, 1), None),
        DroidVisionPoint(Coordinates(0, 1), Enemies(EnemyType.MECH, 2), None),
        DroidVisionPoint(Coordinates(0, 2), Enemies(EnemyType.MECH, 3), None)
      )
    )
    val result = DroidRadarScan.closestEnemies(DroidRadarScan.filterPointsFarThan100Km(droidRadarScan))
    expect(result.map(_.coordinates) == List(Coordinates(0, 1), Coordinates(0, 2), Coordinates(0, 10)))

  }

  pureTest("should find the furthest enemy") {
    val droidRadarScan = DroidRadarScan(
      List.empty,
      List(
        DroidVisionPoint(Coordinates(0, 10), Enemies(EnemyType.MECH, 1), None),
        DroidVisionPoint(Coordinates(0, 1), Enemies(EnemyType.MECH, 2), None),
        DroidVisionPoint(Coordinates(0, 2), Enemies(EnemyType.MECH, 3), None)
      )
    )
    val result = DroidRadarScan.furthestEnemies(DroidRadarScan.filterPointsFarThan100Km(droidRadarScan))
    expect(result.map(_.coordinates) == List(Coordinates(0, 10), Coordinates(0, 2), Coordinates(0, 1)))
  }

  pureTest("should assist the highest number of allies") {
    val droidRadarScan = DroidRadarScan(
      List.empty,
      List(
        DroidVisionPoint(Coordinates(0, 10), Enemies(EnemyType.MECH, 1), Some(1)),
        DroidVisionPoint(Coordinates(0, 1), Enemies(EnemyType.MECH, 2), Some(2)),
        DroidVisionPoint(Coordinates(0, 2), Enemies(EnemyType.MECH, 3), Some(3))
      )
    )
    val result = DroidRadarScan.assistAllies(DroidRadarScan.filterPointsFarThan100Km(droidRadarScan))
    expect(result.map(_.coordinates) == List(Coordinates(0, 2), Coordinates(0, 1), Coordinates(0, 10)))
  }

  pureTest("should avoid crossfire") {
    val droidRadarScan = DroidRadarScan(
      List.empty,
      List(
        DroidVisionPoint(Coordinates(0, 10), Enemies(EnemyType.MECH, 1), Some(1)),
        DroidVisionPoint(Coordinates(0, 1), Enemies(EnemyType.MECH, 2), Some(5)),
        DroidVisionPoint(Coordinates(0, 2), Enemies(EnemyType.MECH, 3), None)
      )
    )
    val result = DroidRadarScan.avoidCrossFire(DroidRadarScan.filterPointsFarThan100Km(droidRadarScan))
    expect(result.map(_.coordinates) == List(Coordinates(0, 2)))
  }

  pureTest("should prioritize mech") {
    val droidRadarScan = DroidRadarScan(
      List.empty,
      List(
        DroidVisionPoint(Coordinates(0, 10), Enemies(EnemyType.MECH, 1), Some(1)),
        DroidVisionPoint(Coordinates(0, 1), Enemies(EnemyType.SOLDIER, 2), None),
        DroidVisionPoint(Coordinates(0, 2), Enemies(EnemyType.MECH, 3), Some(3))
      )
    )
    val result = DroidRadarScan.prioritizeMech(DroidRadarScan.filterPointsFarThan100Km(droidRadarScan))
    expect(result.map(_.coordinates) == List(Coordinates(0, 2), Coordinates(0, 10)))
  }

  test("should filter points far than 100 km") {
    forall(generators.genBigDistanceScan) { droidRadarScan =>
      val result = DroidRadarScan.filterPointsFarThan100Km(droidRadarScan)
      expect.all(
        result.scan.forall(point =>
          Coordinates.distance(point.coordinates, DroidRadarScan.ORIGIN) <= DroidRadarScan.MAX_DISTANCE
        ),
        // assert the list is sorted by distance from min to max
        result.scan
          .sliding(2)
          .forall {
            case List(point1, point2) =>
              Coordinates.distance(point1.coordinates, DroidRadarScan.ORIGIN) <= Coordinates.distance(
                point2.coordinates,
                DroidRadarScan.ORIGIN
              )
            case _ => true
          }
      )

    }
  }

  pureTest("should find the next target: closest enemies and assist allies") {
    val droidRadarScan = DroidRadarScan(
      List(
        Protocol.CLOSEST_ENEMIES,
        Protocol.ASSIST_ALLIES
      ),
      List(
        DroidVisionPoint(Coordinates(0, 10), Enemies(EnemyType.MECH, 1), Some(100)),
        DroidVisionPoint(Coordinates(0, 1), Enemies(EnemyType.MECH, 2), None),
        DroidVisionPoint(Coordinates(0, 2), Enemies(EnemyType.MECH, 3), Some(10))
      )
    )
    val result = DroidRadarScan.findNextTarget(droidRadarScan)
    expect(result.map(_.coordinates) == Some(Coordinates(0, 2)))
  }

  pureTest("should find the next target: closest enemies and avoid crossfire") {
    val droidRadarScan = DroidRadarScan(
      List(
        Protocol.CLOSEST_ENEMIES,
        Protocol.AVOID_CROSSFIRE
      ),
      List(
        DroidVisionPoint(Coordinates(0, 10), Enemies(EnemyType.MECH, 1), Some(100)),
        DroidVisionPoint(Coordinates(0, 1), Enemies(EnemyType.MECH, 2), Some(5)),
        DroidVisionPoint(Coordinates(0, 2), Enemies(EnemyType.MECH, 3), None)
      )
    )
    val result = DroidRadarScan.findNextTarget(droidRadarScan)
    expect(result.map(_.coordinates) == Some(Coordinates(0, 2)))
  }

  pureTest("should find the next target: closest enemies and prioritize mech") {
    val droidRadarScan = DroidRadarScan(
      List(
        Protocol.CLOSEST_ENEMIES,
        Protocol.PRIORITIZE_MECH
      ),
      List(
        DroidVisionPoint(Coordinates(0, 10), Enemies(EnemyType.MECH, 1), Some(100)),
        DroidVisionPoint(Coordinates(0, 1), Enemies(EnemyType.SOLDIER, 2), None),
        DroidVisionPoint(Coordinates(0, 2), Enemies(EnemyType.MECH, 3), Some(10))
      )
    )
    val result = DroidRadarScan.findNextTarget(droidRadarScan)
    expect(result.map(_.coordinates) == Some(Coordinates(0, 2)))
  }

  pureTest("should find the next target: closest enemies and avoid mech") {
    val droidRadarScan = DroidRadarScan(
      List(
        Protocol.CLOSEST_ENEMIES,
        Protocol.AVOID_MECH
      ),
      List(
        DroidVisionPoint(Coordinates(0, 10), Enemies(EnemyType.MECH, 1), Some(100)),
        DroidVisionPoint(Coordinates(0, 1), Enemies(EnemyType.MECH, 2), Some(5)),
        DroidVisionPoint(Coordinates(0, 2), Enemies(EnemyType.MECH, 3), None),
        DroidVisionPoint(Coordinates(0, 4), Enemies(EnemyType.SOLDIER, 3), None)
      )
    )
    val result = DroidRadarScan.findNextTarget(droidRadarScan)
    expect(result.map(_.coordinates) == Some(Coordinates(0, 4)))
  }

  pureTest("should find the next target: closest enemies, prioritize mech and avoid crossfire") {
    val droidRadarScan = DroidRadarScan(
      List(
        Protocol.CLOSEST_ENEMIES,
        Protocol.PRIORITIZE_MECH,
        Protocol.AVOID_CROSSFIRE
      ),
      List(
        DroidVisionPoint(Coordinates(0, 10), Enemies(EnemyType.MECH, 1), Some(100)),
        DroidVisionPoint(Coordinates(0, 1), Enemies(EnemyType.MECH, 2), Some(5)),
        DroidVisionPoint(Coordinates(0, 2), Enemies(EnemyType.SOLDIER, 3), None),
        DroidVisionPoint(Coordinates(0, 4), Enemies(EnemyType.MECH, 2), None)
      )
    )
    val result = DroidRadarScan.findNextTarget(droidRadarScan)
    expect(result.map(_.coordinates) == Some(Coordinates(0, 4)))
  }

  pureTest("should find the next target: furthest enemies and assist allies") {
    val droidRadarScan = DroidRadarScan(
      List(
        Protocol.FURTHEST_ENEMIES,
        Protocol.ASSIST_ALLIES
      ),
      List(
        DroidVisionPoint(Coordinates(0, 10), Enemies(EnemyType.MECH, 1), Some(12)),
        DroidVisionPoint(Coordinates(0, 1), Enemies(EnemyType.MECH, 2), Some(99)),
        DroidVisionPoint(Coordinates(0, 2), Enemies(EnemyType.MECH, 3), Some(10))
      )
    )
    val result = DroidRadarScan.findNextTarget(droidRadarScan)
    expect(result.map(_.coordinates) == Some(Coordinates(0, 10)))
  }

  pureTest("should find the next target: furthest enemies and avoid crossfire") {
    val droidRadarScan = DroidRadarScan(
      List(
        Protocol.FURTHEST_ENEMIES,
        Protocol.AVOID_CROSSFIRE
      ),
      List(
        DroidVisionPoint(Coordinates(0, 10), Enemies(EnemyType.MECH, 1), Some(12)),
        DroidVisionPoint(Coordinates(0, 1), Enemies(EnemyType.MECH, 2), Some(99)),
        DroidVisionPoint(Coordinates(0, 2), Enemies(EnemyType.MECH, 3), None)
      )
    )
    val result = DroidRadarScan.findNextTarget(droidRadarScan)
    expect(result.map(_.coordinates) == Some(Coordinates(0, 2)))
  }

  pureTest("should find the next target: furthest enemies and prioritize mech") {
    val droidRadarScan = DroidRadarScan(
      List(
        Protocol.FURTHEST_ENEMIES,
        Protocol.PRIORITIZE_MECH
      ),
      List(
        DroidVisionPoint(Coordinates(0, 10), Enemies(EnemyType.SOLDIER, 1), Some(12)),
        DroidVisionPoint(Coordinates(0, 1), Enemies(EnemyType.SOLDIER, 2), None),
        DroidVisionPoint(Coordinates(0, 2), Enemies(EnemyType.MECH, 3), Some(10))
      )
    )
    val result = DroidRadarScan.findNextTarget(droidRadarScan)
    expect(result.map(_.coordinates) == Some(Coordinates(0, 2)))
  }

  pureTest("should find the next target: furthest enemies and avoid mech") {
    val droidRadarScan = DroidRadarScan(
      List(
        Protocol.FURTHEST_ENEMIES,
        Protocol.AVOID_MECH
      ),
      List(
        DroidVisionPoint(Coordinates(0, 10), Enemies(EnemyType.MECH, 1), Some(12)),
        DroidVisionPoint(Coordinates(0, 1), Enemies(EnemyType.MECH, 2), Some(99)),
        DroidVisionPoint(Coordinates(0, 2), Enemies(EnemyType.SOLDIER, 3), None),
        DroidVisionPoint(Coordinates(0, 7), Enemies(EnemyType.SOLDIER, 2), None)
      )
    )
    val result = DroidRadarScan.findNextTarget(droidRadarScan)
    expect(result.map(_.coordinates) == Some(Coordinates(0, 7)))
  }

  pureTest("should find the next target: furthest enemy, prioritize mech and avoid crossfire") {
    val droidRadarScan = DroidRadarScan(
      List(
        Protocol.FURTHEST_ENEMIES,
        Protocol.PRIORITIZE_MECH,
        Protocol.AVOID_CROSSFIRE
      ),
      List(
        DroidVisionPoint(Coordinates(0, 10), Enemies(EnemyType.MECH, 1), Some(12)),
        DroidVisionPoint(Coordinates(0, 1), Enemies(EnemyType.MECH, 2), Some(99)),
        DroidVisionPoint(Coordinates(0, 2), Enemies(EnemyType.SOLDIER, 3), None),
        DroidVisionPoint(Coordinates(0, 7), Enemies(EnemyType.MECH, 2), None)
      )
    )
    val result = DroidRadarScan.findNextTarget(droidRadarScan)
    expect(result.map(_.coordinates) == Some(Coordinates(0, 7)))
  }
