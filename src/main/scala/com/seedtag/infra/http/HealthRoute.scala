package com.seedtag.infra.http

import cats.Monad

import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

final class HealthRoute[F[_]: Monad] extends Http4sDsl[F]:

  val routes: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root / "health" => Ok()
  }
