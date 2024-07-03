package com.seedtag.domain

import cats.effect.implicits.*
import cats.effect.kernel.Async
import cats.implicits.*

class AttackService[F[_]: Async](
    cannon1: IonCannonClient[F],
    cannon2: IonCannonClient[F],
    cannon3: IonCannonClient[F]
):
  def attack(droidRadarScan: DroidRadarScan): F[Report] =
    for
      nextTarget      <- findNextTarget(droidRadarScan)
      ionCannon       <- findBestAvailableIonCannon()
      damageInflicted <- fireIonCannon(ionCannon, nextTarget)
      report          <- report(nextTarget, damageInflicted)
    yield report

  def findNextTarget(droidRadarScan: DroidRadarScan): F[DroidVisionPoint] =
    DroidRadarScan.findNextTarget(droidRadarScan) match
      case Some(target) => target.pure[F]
      case None         => Async[F].raiseError[DroidVisionPoint](new Exception("No target found"))

  def findBestAvailableIonCannon(): F[IonCannon] =
    (cannon1.status, cannon2.status, cannon3.status).parMapN { (c1, c2, c3) =>
      List(c1, c2, c3).filter(_.available).minBy(_.generation)
    }

  def fireIonCannon(ionCannon: IonCannon, target: DroidVisionPoint): F[DamageInflicted] =
    val command = FireCommand.fromTarget(target)

    ionCannon.generation match
      case 1 => cannon1.fire(command)
      case 2 => cannon2.fire(command)
      case 3 => cannon3.fire(command)
      case _ => Async[F].raiseError[DamageInflicted](new Exception("Invalid generation"))

  def report(target: DroidVisionPoint, damageInflicted: DamageInflicted): F[Report] =
    Report(target, damageInflicted).pure[F]
