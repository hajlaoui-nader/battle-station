package com.seedtag

import cats.effect.kernel.Resource
import cats.effect.{ IO, IOApp }

import com.seedtag.domain.AttackService
import com.seedtag.infra.client.IonCannonClient
import com.seedtag.infra.http.{ AttackRoute, Ember }
import com.seedtag.infra.resources.*
import org.typelevel.log4cats.*
import org.typelevel.log4cats.slf4j.*

object Main extends IOApp.Simple:

  given Logger[IO]        = Slf4jLogger.getLogger[IO]
  given LoggerFactory[IO] = Slf4jFactory.create[IO]

  def run: IO[Unit] =
    resources.useForever

  def resources =
    for
      appConfig    <- Resource.eval { AppConfig.load[IO] }
      appResources <- AppResources.make[IO](appConfig)
      cannon1 = IonCannonClient.make[IO](appConfig.cannons.cannon1, appResources.client)
      cannon2 = IonCannonClient.make[IO](appConfig.cannons.cannon2, appResources.client)
      cannon3 = IonCannonClient.make[IO](appConfig.cannons.cannon3, appResources.client)

      routes = AttackRoute[IO](new AttackService[IO](cannon1, cannon2, cannon3)).routes
      server <- Ember.routes[IO](
        appConfig.server.host,
        appConfig.server.port,
        routes
      )
    yield server
