package com.seedtag.domain

enum EnemyType:
  case SOLDIER
  case MECH

case class Enemies(
    `type`: EnemyType,
    number: Int
)
