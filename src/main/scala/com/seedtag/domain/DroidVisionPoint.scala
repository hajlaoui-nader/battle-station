package com.seedtag.domain

case class DroidVisionPoint(
    coordinates: Coordinates,
    enemies: Enemies,
    allies: Option[Int]
)

case class Coordinates(
    x: Int,
    y: Int
)

object Coordinates:
  def distance(point1: Coordinates, point2: Coordinates): Double =
    Math.hypot(point1.x - point2.x, point1.y - point2.y)

case class DroidRadarScan(
    protocols: List[Protocol],
    scan: List[DroidVisionPoint]
)

object DroidRadarScan:
  val ORIGIN       = Coordinates(0, 0)
  val MAX_DISTANCE = 100

  private def firstCommonTarget[T](listOfTargets: List[List[T]]): Option[T] =
    listOfTargets match
      case Nil => None
      case head :: tail =>
        val common = head.find(elem => tail.forall(_.contains(elem)))
        common.orElse(firstCommonTarget(tail))

  def findNextTarget(droidRadarScan: DroidRadarScan): Option[DroidVisionPoint] =
    val scan = filterPointsFarThan100Km(droidRadarScan)

    val targets = scan.protocols.map {

      case Protocol.CLOSEST_ENEMIES => closestEnemies(scan)

      case Protocol.FURTHEST_ENEMIES => furthestEnemies(scan)

      case Protocol.ASSIST_ALLIES => assistAllies(scan)

      case Protocol.AVOID_CROSSFIRE => avoidCrossFire(scan)

      case Protocol.PRIORITIZE_MECH => prioritizeMech(scan)

      case Protocol.AVOID_MECH => avoidMech(scan)
    }

    firstCommonTarget(targets)

  // filter points far than 100 km and sort by distance
  def filterPointsFarThan100Km(droidRadarScan: DroidRadarScan): DroidRadarScan =
    // sort by distance and filter
    val filteredPoints =
      droidRadarScan.scan
        .map(point => (point, Coordinates.distance(point.coordinates, ORIGIN)))
        .filter { case (point, distance) => distance <= MAX_DISTANCE }
        .sortBy(_._2)
        .map(_._1)

    droidRadarScan.copy(scan = filteredPoints)

  // the next functions operates on a sorted scan
  // the first element is the closest point to the origin
  def closestEnemies(droidRadarScan: DroidRadarScan): List[DroidVisionPoint] =
    droidRadarScan.scan

  // the first element is the furthest point from the origin
  def furthestEnemies(droidRadarScan: DroidRadarScan): List[DroidVisionPoint] =
    droidRadarScan
      .scan
      .reverse

  // the first element is the point with more allies
  def assistAllies(droidRadarScan: DroidRadarScan): List[DroidVisionPoint] =
    droidRadarScan
      .scan
      .filter(_.allies.isDefined)
      .sortBy(-_.allies.getOrElse(0))

  // the first element is the point with less allies
  def avoidCrossFire(droidRadarScan: DroidRadarScan): List[DroidVisionPoint] =
    droidRadarScan
      .scan
      .filter(_.allies.isEmpty)
      .sortBy(_.allies.getOrElse(0))

  // the first element is the nearest mech
  def prioritizeMech(droidRadarScan: DroidRadarScan): List[DroidVisionPoint] =
    droidRadarScan.scan.filter(_.enemies.`type` == EnemyType.MECH)

  // the first element is the nearest soldier
  def avoidMech(droidRadarScan: DroidRadarScan): List[DroidVisionPoint] =
    droidRadarScan
      .scan
      .filter(_.enemies.`type` != EnemyType.MECH)
