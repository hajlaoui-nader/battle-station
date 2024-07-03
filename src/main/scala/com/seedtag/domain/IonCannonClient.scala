package com.seedtag.domain

trait IonCannonClient[F[_]]:
  def status: F[IonCannon]
  def fire(command: FireCommand): F[DamageInflicted]
