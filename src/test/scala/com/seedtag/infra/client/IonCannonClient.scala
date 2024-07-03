package com.seedtag.infra.client

import cats.Show
import cats.effect.IO

import com.comcast.ip4s.{ Hostname, Port }
import com.seedtag.domain.generators
import com.seedtag.{ HttpConfig, domain }
import io.scalaland.chimney.dsl.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.client.Client
import org.http4s.dsl.io.*
import org.http4s.implicits.*
import org.http4s.{ HttpApp, HttpRoutes, Response }
import weaver.*
import weaver.scalacheck.Checkers

object IonCannonClientSuite extends SimpleIOSuite with Checkers:

  private def config = HttpConfig(
    Hostname.fromString("localhost").get,
    Port.fromString("8080").get
  )

  given showIonCannon: Show[domain.IonCannon]             = Show.fromToString
  given showDamageInflicted: Show[domain.DamageInflicted] = Show.fromToString
  given showFireCommand: Show[domain.FireCommand]         = Show.fromToString

  def testRoutes(mkResponse: IO[Response[IO]]): HttpApp[IO] =
    HttpRoutes
      .of[IO] {
        case GET -> Root / "status" =>
          mkResponse

        case POST -> Root / "fire" =>
          mkResponse
      }
      .orNotFound

  test("/status response OK [200]") {
    forall(generators.genAvailableIonCannon) { ionCannon =>
      val route  = testRoutes(Ok(ionCannon.transformInto[IonCannon]))
      val client = Client.fromHttpApp(route)

      IonCannonClient.make[IO](config, client)
        .status
        .map(expect.same(ionCannon, _))
    }
  }

  test("/status Internal Server Error response (500)") {
    forall(generators.genAvailableIonCannon) { ionCannon =>
      val client = Client.fromHttpApp(testRoutes(InternalServerError()))

      IonCannonClient.make[IO](config, client)
        .status
        .attempt
        .map {
          case Left(e)  => expect.same(domain.IonCannonCommunicationError("Internal Server Error"), e)
          case Right(_) => failure("expected ion cannon error")
        }
    }
  }

  test("/fire response OK [200]") {
    val gen =
      for
        fireCommand     <- generators.genFireCommand
        damageInflicted <- generators.genDamageInflicted
      yield (fireCommand, damageInflicted)
    forall(gen) { case (fireCommand, damageInflicted) =>
      val route  = testRoutes(Ok(damageInflicted.transformInto[DamageInflicted]))
      val client = Client.fromHttpApp(route)

      IonCannonClient.make[IO](config, client)
        .fire(fireCommand)
        .map(expect.same(damageInflicted, _))
    }
  }

  test("/fire Internal Server Error response (500)") {
    forall(generators.genFireCommand) { fireCommand =>
      val client = Client.fromHttpApp(testRoutes(InternalServerError()))

      IonCannonClient.make[IO](config, client)
        .fire(fireCommand)
        .attempt
        .map {
          case Left(e)  => expect.same(domain.IonCannonCommunicationError("Internal Server Error"), e)
          case Right(_) => failure("expected ion cannon error")
        }
    }
  }
