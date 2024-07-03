package com.seedtag.infra.client

import cats.effect.MonadCancelThrow
import cats.implicits.*

import com.seedtag.domain.IonCannonCommunicationError
import com.seedtag.{ HttpConfig, domain as domain }
import io.circe.syntax.*
import io.scalaland.chimney.dsl.*
import org.http4s.Method.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl

object IonCannonClient:
  def make[F[_]: JsonDecoder: MonadCancelThrow](
      cannonConfig: HttpConfig,
      client: Client[F]
  ): domain.IonCannonClient[F] =
    new domain.IonCannonClient[F] with Http4sClientDsl[F]:
      def status: F[domain.IonCannon] =
        Uri.fromString(s"http://${cannonConfig.host.toString}:${cannonConfig.port.value}/status").liftTo[F].flatMap {
          uri =>
            client.run(GET(uri)).use { resp =>
              resp.status match
                case Status.Ok =>
                  resp.asJsonDecode[IonCannon].map(_.transformInto[domain.IonCannon])
                case st =>
                  IonCannonCommunicationError(
                    Option(st.reason).getOrElse("unknown")
                  ).raiseError[F, domain.IonCannon]
            }
        }

      def fire(command: domain.FireCommand): F[domain.DamageInflicted] =
        Uri.fromString(s"http://${cannonConfig.host.toString}:${cannonConfig.port.value}/fire").liftTo[F].flatMap {
          uri =>
            client.run(POST(command.transformInto[FireCommand].asJson, uri)).use { resp =>
              resp.status match
                case Status.Ok =>
                  resp.asJsonDecode[DamageInflicted].map(_.transformInto[domain.DamageInflicted])
                case st =>
                  IonCannonCommunicationError(
                    Option(st.reason).getOrElse("unknown")
                  ).raiseError[F, domain.DamageInflicted]
            }
        }
