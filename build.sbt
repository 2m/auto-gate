organization := "lt.dvim.auto-gate"
name := "auto-gate"
description := "Uses Twilio to make a call to garage opening number"

scalaVersion := "2.13.3"
val Tapir = "0.17.4"
val Circe = "0.13.0"

libraryDependencies ++= Seq(
  "com.google.cloud.functions"     % "functions-framework-api" % "1.0.3"  % "provided",
  "com.softwaremill.sttp.tapir"   %% "tapir-core"              % Tapir,
  "com.softwaremill.sttp.tapir"   %% "tapir-json-circe"        % Tapir,
  "com.softwaremill.sttp.tapir"   %% "tapir-sttp-client"       % Tapir,
  "com.softwaremill.sttp.client3" %% "httpclient-backend"      % "3.0.0",
  "is.cir"                        %% "ciris-core"              % "0.13.0-RC1",
  "lt.dvim.ciris-hocon"           %% "ciris-hocon"             % "0.2",
  "io.circe"                      %% "circe-core"              % Circe,
  "org.scalameta"                 %% "munit"                   % "0.7.20" % Test,
  "com.typesafe.akka"             %% "akka-stream"             % "2.6.11" % Test
)

testFrameworks += new TestFramework("munit.Framework")

scalafmtOnCompile := true
scalafixOnCompile := true

ThisBuild / scalafixDependencies ++= Seq(
  "com.nequissimus" %% "sort-imports" % "0.5.5"
)

enablePlugins(AutomateHeaderPlugin)
startYear := Some(2020)
organizationName := "github.com/2m/auto-gate/contributors"
licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt"))

InputKey[Unit]("gcDeploy") := {
  import scala.sys.process._

  val projectId = "autogate-e554a"

  s"""|gcloud functions deploy ${name.value}
      |  --runtime java11
      |  --entry-point ${(Compile / mainClass).value.get}
      |  --source ${crossTarget.value}
      |  --trigger-event providers/cloud.firestore/eventTypes/document.update
      |  --trigger-resource projects/$projectId/databases/(default)/documents/users/{userId}/devices/{deviceId}
  """.stripMargin !
}
