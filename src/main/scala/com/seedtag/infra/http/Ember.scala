package com.seedtag.infra.http

import scala.concurrent.duration.*

import cats.effect.kernel.{ Async, Concurrent, Resource }
import cats.effect.std.Console
import cats.syntax.all.*

import com.comcast.ip4s.*
import fs2.io.net.Network
import org.http4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.http4s.server.Server
import org.http4s.server.defaults.Banner
import org.http4s.server.middleware.*
import org.typelevel.log4cats.LoggerFactory

object Ember:
  private def showBanner[F[_]: Console](s: Server): F[Unit] =
    Console[F].println(s"\n${Banner.mkString("\n")}\nHTTP Server started at ${s.address}")

  private def make[F[_]: Async: Network: LoggerFactory](host: Hostname, port: Port): EmberServerBuilder[F] =
    EmberServerBuilder.default[F].withHost(host).withPort(port)

  def routes[F[_]: Async: Console: Network: Concurrent: LoggerFactory](
      host: Hostname,
      port: Port,
      routes: HttpRoutes[F]
  ): Resource[F, Server] =

    val middleware: HttpRoutes[F] => HttpRoutes[F] = { (http: HttpRoutes[F]) =>
      AutoSlash(http)
    } andThen { (http: HttpRoutes[F]) =>
      // TODO [IMPORTANT] - CORS policy should be configured properly
      val cors = CORS.policy.withAllowOriginAll
      cors.apply(http)
    } andThen { (http: HttpRoutes[F]) =>
      Timeout(60.seconds)(http)
    }

    val loggers: HttpApp[F] => HttpApp[F] = { (http: HttpApp[F]) =>
      RequestLogger.httpApp(true, true)(http)
    } andThen { (http: HttpApp[F]) =>
      ResponseLogger.httpApp(true, true)(http)
    }

    make[F](host, port)
      .withHttpApp(loggers(middleware(HealthRoute[F].routes <+> routes).orNotFound))
      .build
      .evalTap(showBanner[F])
