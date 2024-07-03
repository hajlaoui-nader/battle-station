package com.seedtag.infra.resources

import scala.concurrent.duration.*

import cats.effect.kernel.{ Async, Resource }

import fs2.io.net.Network
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder

// Just to demonstrate how far we can take this pattern and void hard constraints like Async
trait MkHttpClient[F[_]]:
  def newEmber(): Resource[F, Client[F]]

object MkHttpClient:
  def apply[F[_]: MkHttpClient]: MkHttpClient[F] = implicitly

  // TODO fix timeout
  implicit def forAsync[F[_]: Async: Network]: MkHttpClient[F] =
    new MkHttpClient[F]:
      def newEmber(): Resource[F, Client[F]] =
        EmberClientBuilder
          .default[F]
          .withTimeout(1.second)
          // .withIdleTimeInPool(1.second)
          .build
