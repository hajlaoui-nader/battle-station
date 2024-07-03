package com.seedtag
import cats.effect.kernel.Async
import cats.syntax.all.*

import ciris.*
import com.comcast.ip4s.*

case class AppConfig(server: HttpConfig, cannons: CannonsConfig)

case class HttpConfig(
    host: Hostname,
    port: Port
)

case class CannonsConfig(
    cannon1: HttpConfig,
    cannon2: HttpConfig,
    cannon3: HttpConfig
)

object AppConfig:
  def load[F[_]: Async]: F[AppConfig] =
    (
      env("HTTP_HOST").as[String].default("0.0.0.0"),
      env("HTTP_PORT").as[String].default("3000"),
      env("CANNON1_HOST").as[String].default("0.0.0.0"),
      env("CANNON1_PORT").as[String].default("3001"),
      env("CANNON2_HOST").as[String].default("0.0.0.0"),
      env("CANNON2_PORT").as[String].default("3002"),
      env("CANNON3_HOST").as[String].default("0.0.0.0"),
      env("CANNON3_PORT").as[String].default("3003")
    )
      .parMapN { (httpHost, httpPort, canonn1Host, cannon1Port, cannon2Host, cannon2Port, cannon3Host, cannon3Port) =>
        AppConfig(
          HttpConfig(
            Hostname.fromString(httpHost).getOrElse(host"0.0.0.0"),
            Port.fromString(httpPort).getOrElse(port"8080")
          ),
          CannonsConfig(
            HttpConfig(
              Hostname.fromString(canonn1Host).getOrElse(host"0.0.0.0"),
              Port.fromString(cannon1Port).getOrElse(port"3001")
            ),
            HttpConfig(
              Hostname.fromString(cannon2Host).getOrElse(host"0.0.0.0"),
              Port.fromString(cannon2Port).getOrElse(port"3002")
            ),
            HttpConfig(
              Hostname.fromString(cannon3Host).getOrElse(host"0.0.0.0"),
              Port.fromString(cannon3Port).getOrElse(port"3003")
            )
          )
        )
      }.load[F]
