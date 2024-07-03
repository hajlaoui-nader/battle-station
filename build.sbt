import Dependencies._
import sbtwelcome._
import com.typesafe.sbt.SbtNativePackager.autoImport.NativePackagerHelper._

ThisBuild / organization     := "com.seedtag"
ThisBuild / organizationName := "seedtag"
ThisBuild / scalaVersion     := "3.3.0"

Global / onChangedBuildSource := ReloadOnSourceChanges
Global / semanticdbEnabled    := true // for metals
// mappings in Universal ++= directory(baseDirectory.value / "data")
addCommandAlias("lint", ";scalafmtAll ;scalafixAll --rules OrganizeImports")

ThisBuild / assemblyMergeStrategy := {
  case PathList("module-info.class") => MergeStrategy.first
  case x =>
    val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    oldStrategy(x)
}
lazy val root = (project in file("."))
  .settings(
    name := "battle-station",
    libraryDependencies ++= Seq(
      // cats
      Libraries.cats,
      Libraries.catsEffect,
      Libraries.catsEffectKernel,
      // circe
      Libraries.circeCore,
      Libraries.circeParser,
      Libraries.circeRefined,
      // http4s
      Libraries.http4sDsl,
      Libraries.http4sServer,
      Libraries.http4sCirce,
      Libraries.http4sClient,
      Libraries.ip4sCore,
      // fs2
      Libraries.fs2Core,
      Libraries.fs2Io,
      // chimney
      Libraries.chimney,
      // log
      Libraries.log4catsNoop,
      Libraries.log4cats,
      Libraries.logback,
      // config
      Libraries.cirisCore,
      // tests
      Libraries.weaver,
      Libraries.weaverScalacheck,
      Libraries.scalacheck
    ),
    testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
    Defaults.itSettings,
    Compile / mainClass := Some("com.seedtag.Main")
  )
  .configs(IntegrationTest)
  .enablePlugins(AshScriptPlugin) // alternate to JavaAppPackaging designed to support the ash shell.
  .enablePlugins(DockerPlugin)
  .enablePlugins(GraalVMNativeImagePlugin)
  .enablePlugins(NativeImagePlugin)
  .settings(dockerSettings("battle-station"))

def dockerSettings(name: String) = List(
  Docker / packageName := name,
  dockerBaseImage      := "amazoncorretto:17-alpine-jdk",
  dockerExposedPorts ++= List(3000),
  makeBatScripts       := Nil,
  dockerUpdateLatest   := true,
  dockerExposedVolumes := Seq("/tmp/seedtag/")
)
usefulTasks := List(
  UsefulTask("lint", "Run scalafmtAll and scalafix OrganizeImports rule"),
  UsefulTask("IntegrationTest/test", "Run all integration tests")
)

logo := s"""
   |Powered by ${scala.Console.YELLOW}Scala ${scalaVersion.value}${scala.Console.RESET}
   |Project: ${scala.Console.CYAN}${name.value} ${scala.Console.RESET}
██████╗░░█████╗░████████╗████████╗██╗░░░░░███████╗░░░░░░░██████╗████████╗░█████╗░████████╗██╗░█████╗░███╗░░██╗
██╔══██╗██╔══██╗╚══██╔══╝╚══██╔══╝██║░░░░░██╔════╝░░░░░░██╔════╝╚══██╔══╝██╔══██╗╚══██╔══╝██║██╔══██╗████╗░██║
██████╦╝███████║░░░██║░░░░░░██║░░░██║░░░░░█████╗░░█████╗╚█████╗░░░░██║░░░███████║░░░██║░░░██║██║░░██║██╔██╗██║
██╔══██╗██╔══██║░░░██║░░░░░░██║░░░██║░░░░░██╔══╝░░╚════╝░╚═══██╗░░░██║░░░██╔══██║░░░██║░░░██║██║░░██║██║╚████║
██████╦╝██║░░██║░░░██║░░░░░░██║░░░███████╗███████╗░░░░░░██████╔╝░░░██║░░░██║░░██║░░░██║░░░██║╚█████╔╝██║░╚███║
╚═════╝░╚═╝░░╚═╝░░░╚═╝░░░░░░╚═╝░░░╚══════╝╚══════╝░░░░░░╚═════╝░░░░╚═╝░░░╚═╝░░╚═╝░░░╚═╝░░░╚═╝░╚════╝░╚═╝░░╚══╝
""".stripMargin
