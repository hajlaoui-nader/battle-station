package com.seedtag.infra.http

import cats.effect.kernel.Concurrent
import cats.syntax.all.*

import com.seedtag.domain
import com.seedtag.domain.AttackService
import io.circe.syntax.*
import io.scalaland.chimney.dsl.*
import org.http4s.HttpRoutes
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.io.*

final class AttackRoute[F[_]: Concurrent](service: AttackService[F]) extends Http4sDsl[F]:

  import org.http4s.circe.CirceEntityCodec.circeEntityDecoder

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case contextRequest @ POST -> Root / "attack" =>
      for
        command  <- contextRequest.as[AttackCommand]
        report   <- service.attack(command.transformInto[domain.DroidRadarScan])
        response <- Ok(report.transformInto[Report].asJson)
      yield response

  }
