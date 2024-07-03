package com.seedtag.infra.http

import cats.Show
import cats.effect.IO

import com.seedtag.domain
import com.seedtag.domain.generators
import io.scalaland.chimney.dsl.*
import org.http4s.{ Method, Request, Status, Uri }
import org.typelevel.log4cats.noop.NoOpLogger
import org.typelevel.log4cats.{ Logger, * }
import weaver.*
import weaver.scalacheck.Checkers

given Logger[IO] = NoOpLogger.impl[IO]

object AttackRouteSuite extends HttpSuite with Checkers:
  private val rawAttackCommand = """
  {
        "protocols": ["avoid-mech"],
        "scan":[
                {
                        "coordinates": { "x": 0, "y": 40 },
                        "enemies": { "type": "soldier", "number": 10 }
                }
        ]
}
  """.stripMargin

  given showReport: Show[domain.Report] = Show.fromToString

  test("POST attack suceeds") {
    forall(generators.genReport) { report =>
      val request = Request[IO](
        Method.POST,
        Uri.unsafeFromString(s"/attack"),
        body = fs2.Stream.emits(rawAttackCommand.getBytes().toList)
      )
      val routes = AttackRoute[IO](attackService(report)).routes

      expectHttpBodyAndStatus(routes, request)(
        report.transformInto[Report],
        Status.Ok
      )
    }
  }

  def attackService(expected: domain.Report) = new TestAttack:
    override def attack(scan: domain.DroidRadarScan): IO[domain.Report] =
      IO.pure(expected)

protected class TestAttack extends domain.AttackService[IO](null, null, null):
  override def attack(scan: domain.DroidRadarScan): IO[domain.Report] = ???
