package com.seedtag.domain

import scala.collection.mutable.ListBuffer

import cats.Show
import cats.effect.IO

import com.seedtag.domain
import weaver.*
import weaver.scalacheck.Checkers

object AttackServiceSuite extends SimpleIOSuite with Checkers:

  given showScan: Show[domain.DroidRadarScan]    = Show.fromToString
  given showIonCannon: Show[domain.IonCannon]    = Show.fromToString
  given showPoint: Show[domain.DroidVisionPoint] = Show.fromToString
  given showDamage: Show[domain.DamageInflicted] = Show.fromToString

  test("should find next target") {
    forall(generators.genDroidRadarScan) { scan =>
      val attackService =
        new domain.AttackService[IO](new IonCannonTestClient, new IonCannonTestClient, new IonCannonTestClient)
      attackService.findNextTarget(scan).map { target =>
        expect.same(domain.DroidRadarScan.findNextTarget(scan).get, target)
      }
    }
  }

  test("should find best ion cannon") {
    val gen =
      for
        cannon1 <- generators.genAvailableIonCannon
        cannon2 <- generators.genAvailableIonCannon.retryUntil(_ != cannon1)
        cannon3 <- generators.genAvailableIonCannon.retryUntil(cannon => cannon != cannon1 && cannon != cannon2)
      yield (cannon1, cannon2, cannon3)

    forall(gen) { case (ionCannon1, ionCannon2, ionCannon3) =>
      val attackService =
        new domain.AttackService[IO](ionCannon(ionCannon1), ionCannon(ionCannon2), ionCannon(ionCannon3))

      attackService.findBestAvailableIonCannon().map { ionCannon =>
        expect.all(
          ionCannon == List(ionCannon1, ionCannon2, ionCannon3).minBy(_.generation),
          ionCannon.available == true
        )
      }
    }
  }

  test("should fail to find best ion cannon when all cannons fail") {
    val attackService =
      new domain.AttackService[IO](
        failingIonCannon,
        failingIonCannon,
        failingIonCannon
      )

    attackService
      .findBestAvailableIonCannon()
      .attempt
      .map {
        case Left(_)  => success
        case Right(_) => failure("Should have failed")
      }
  }

  test("should fail to find best ion cannon when one cannon fails") {
    val gen =
      for
        cannon1 <- generators.genAvailableIonCannon
        cannon2 <- generators.genAvailableIonCannon.retryUntil(_ != cannon1)
      yield (cannon1, cannon2)

    forall(gen) { case (cannon1, cannon2) =>
      val attackService =
        new domain.AttackService[IO](
          ionCannon(cannon1),
          ionCannon(cannon2),
          failingIonCannon
        )

      attackService
        .findBestAvailableIonCannon()
        .attempt
        .map {
          case Left(_)  => success
          case Right(_) => failure("Should have failed")
        }
    }
  }

  test("should fire cannon 1") {
    val gen =
      for
        point  <- generators.genDroidVisionPoint
        damage <- generators.genDamageInflicted
      yield (point, damage)

    forall(gen) { case (point, damage) =>
      val cannon1 = domain.IonCannon(1, true)
      val cannon2 = domain.IonCannon(2, true)
      val cannon3 = domain.IonCannon(3, true)

      val cannonClient1 = fire(damage)
      val attackService =
        new domain.AttackService[IO](cannonClient1, ionCannon(cannon2), ionCannon(cannon3))

      attackService
        .fireIonCannon(cannon1, point)
        .map { damageInflicted =>
          expect.all(
            damageInflicted == damage,
            cannonClient1.getFireCommands().head == domain.FireCommand.fromTarget(point)
          )
        }
    }
  }

  test("should fire cannon 2") {
    val gen =
      for
        point  <- generators.genDroidVisionPoint
        damage <- generators.genDamageInflicted
      yield (point, damage)

    forall(gen) { case (point, damage) =>
      val cannon1 = domain.IonCannon(1, true)
      val cannon2 = domain.IonCannon(2, true)
      val cannon3 = domain.IonCannon(3, true)

      val cannonClient2 = fire(damage)
      val attackService =
        new domain.AttackService[IO](ionCannon(cannon1), cannonClient2, ionCannon(cannon3))

      attackService
        .fireIonCannon(cannon2, point)
        .map { damageInflicted =>
          expect.all(
            damageInflicted == damage,
            cannonClient2.getFireCommands().head == domain.FireCommand.fromTarget(point)
          )
        }
    }
  }

  test("should fire cannon 3") {
    val gen =
      for
        point  <- generators.genDroidVisionPoint
        damage <- generators.genDamageInflicted
      yield (point, damage)

    forall(gen) { case (point, damage) =>
      val cannon1 = domain.IonCannon(1, true)
      val cannon2 = domain.IonCannon(2, true)
      val cannon3 = domain.IonCannon(3, true)

      val cannonClient3 = fire(damage)
      val attackService =
        new domain.AttackService[IO](ionCannon(cannon1), ionCannon(cannon2), cannonClient3)

      attackService
        .fireIonCannon(cannon3, point)
        .map { damageInflicted =>
          expect.all(
            damageInflicted == damage,
            cannonClient3.getFireCommands().head == domain.FireCommand.fromTarget(point)
          )
        }
    }
  }

  test("should report") {
    val gen =
      for
        point  <- generators.genDroidVisionPoint
        damage <- generators.genDamageInflicted
      yield (point, damage)

    forall(gen) { case (point, damage) =>
      val attackService =
        new domain.AttackService[IO](new IonCannonTestClient, new IonCannonTestClient, new IonCannonTestClient)

      attackService
        .report(point, damage)
        .map { report =>
          expect.same(report, domain.Report(point, damage))
        }
    }
  }

  private def ionCannon(ionCannon: domain.IonCannon) = new IonCannonTestClient():
    override def status: IO[domain.IonCannon] = IO.pure(ionCannon)

  private def failingIonCannon = new IonCannonTestClient():
    override def status: IO[domain.IonCannon] = IO.raiseError(new Exception("Error"))

  private def fire(damage: domain.DamageInflicted) =
    new IonCannonTestClient(new ListBuffer()):
      override def fire(command: domain.FireCommand): IO[domain.DamageInflicted] =
        IO.pure(addFireCommand(command)) *> IO.pure(damage)

protected class IonCannonTestClient(fireCommands: ListBuffer[domain.FireCommand] = ListBuffer.empty)
    extends domain.IonCannonClient[IO]:
  override def status: IO[domain.IonCannon]                                  = ???
  override def fire(command: domain.FireCommand): IO[domain.DamageInflicted] = ???
  def getFireCommands(): ListBuffer[domain.FireCommand]                      = fireCommands
  def addFireCommand(command: domain.FireCommand): Unit                      = fireCommands += command
