package com.seedtag.infra.resources

import cats.effect.Concurrent
import cats.effect.kernel.Resource

import com.seedtag.AppConfig
import fs2.io.net.Network
import org.http4s.client.Client
import org.typelevel.log4cats.Logger

sealed abstract class AppResources[F[_]](
    val client: Client[F]
)

object AppResources:
  def make[F[_]: Concurrent: Logger: MkHttpClient: Network](
      cfg: AppConfig
  ): Resource[F, AppResources[F]] =
    for
      client <- MkHttpClient[F].newEmber()
    yield new AppResources[F](client) {}
