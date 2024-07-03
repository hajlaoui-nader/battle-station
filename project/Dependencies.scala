import sbt._

object Dependencies {
  object V {
    val cats       = "2.10.0"
    val catsEffect = "3.5.2"
    val circe      = "0.14.6"
    val chimney    = "0.8.0-RC1"

    // http4s
    val http4s = "0.23.16"
    val ip4s   = "3.3.0"

    // fs2
    val fs2Core = "3.9.1"

    val log4cats = "2.6.0"
    val logback  = "1.4.11"

    // config
    val cirisCore = "3.2.0"

    // tests
    val scalacheck = "1.17.0"
    val weaver     = "0.8.3"
  }

  object Libraries {
    def circe(artifact: String)            = "io.circe"   %% s"circe-$artifact"  % V.circe
    def http4s(artifact: String): ModuleID = "org.http4s" %% s"http4s-$artifact" % V.http4s

    val cats       = "org.typelevel" %% "cats-core"   % V.cats
    val catsEffect = "org.typelevel" %% "cats-effect" % V.catsEffect
    val catsEffectKernel =
      "org.typelevel" %% "cats-effect-kernel" % V.catsEffect

    val circeCore    = circe("core")
    val circeParser  = circe("parser")
    val circeRefined = circe("refined")

    val log4cats     = "org.typelevel" %% "log4cats-slf4j"  % V.log4cats
    val log4catsNoop = "org.typelevel" %% "log4cats-noop"   % V.log4cats
    val logback      = "ch.qos.logback" % "logback-classic" % V.logback

    val fs2Core = "co.fs2" %% "fs2-core" % V.fs2Core
    val fs2Io   = "co.fs2" %% "fs2-io"   % V.fs2Core

    val cirisCore = "is.cir"       %% "ciris"   % V.cirisCore
    val chimney   = "io.scalaland" %% "chimney" % V.chimney

    val http4sDsl    = http4s("dsl")
    val http4sServer = http4s("ember-server")
    val http4sClient = http4s("ember-client")
    val http4sCirce  = http4s("circe")

    val ip4sCore = "com.comcast" %% "ip4s-core" % V.ip4s

    val weaver = "com.disneystreaming" %% "weaver-cats" % V.weaver % "test, it"
    val weaverScalacheck =
      "com.disneystreaming" %% "weaver-scalacheck" % V.weaver % "test, it"
    val scalacheck =
      "org.scalacheck" %% "scalacheck" % V.scalacheck % "test, it"
  }
}
