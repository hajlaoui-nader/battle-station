package com.seedtag.infra.http

import io.circe.*
import io.circe.syntax.*

case class DroidVisionPoint(
    coordinates: Coordinates,
    enemies: Enemies,
    allies: Option[Int]
) derives Codec.AsObject

case class Coordinates(
    x: Int,
    y: Int
) derives Codec.AsObject

case class AttackCommand(
    protocols: List[Protocol],
    scan: List[DroidVisionPoint]
) derives Codec.AsObject

enum EnemyType:
  case SOLDIER
  case MECH

case class Enemies(
    `type`: EnemyType,
    number: Int
) derives Codec.AsObject

object EnemyType:
  given Encoder[EnemyType] = (a: EnemyType) => a.toString.asJson
  given Decoder[EnemyType] = (c: HCursor) =>
    Decoder.decodeString(c).flatMap { str =>
      EnemyType.values.toList.map(enemyType => (enemyType, enemyType.toString.toLowerCase)).find(
        _._2 == str.toLowerCase
      ) match
        case Some(status, _) => Right(status)
        case None =>
          Left(DecodingFailure(s"no enum value matched for $str", List(CursorOp.Field(str))))
    }

enum Protocol(val value: String):
  case CLOSEST_ENEMIES  extends Protocol("closest-enemies")
  case FURTHEST_ENEMIES extends Protocol("furthest-enemies")
  case ASSIST_ALLIES    extends Protocol("assist-allies")
  case AVOID_CROSSFIRE  extends Protocol("avoid-crossfire")
  case PRIORITIZE_MECH  extends Protocol("prioritize-mech")
  case AVOID_MECH       extends Protocol("avoid-mech")

object Protocol:
  given Encoder[Protocol] = (a: Protocol) => a.value.asJson
  given Decoder[Protocol] = (c: HCursor) =>
    Decoder.decodeString(c).flatMap { str =>
      Protocol.values.toList.map(protocol => (protocol, protocol.value)).find(
        _._2 == str.toLowerCase
      ) match
        case Some(status, _) => Right(status)
        case None =>
          Left(DecodingFailure(s"no enum value matched for $str", List(CursorOp.Field(str))))
    }

case class Report(
    target: Coordinates,
    casualties: Int,
    generation: Int
) derives Codec.AsObject
