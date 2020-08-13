organization := "lt.dvim.auto-gate"
name := "auto-gate"
description := "Uses Twilio to make a call to garage opening number"

scalaVersion := "2.13.3"

libraryDependencies ++= Seq(
  "com.google.cloud.functions" % "functions-framework-api" % "1.0.1" % "provided"
)

scalafmtOnCompile := true
scalafixOnCompile := true

ThisBuild / scalafixDependencies ++= Seq(
  "com.nequissimus" %% "sort-imports" % "0.5.4"
)

enablePlugins(AutomateHeaderPlugin)
startYear := Some(2020)
organizationName := "github.com/2m/auto-gate/contributors"
licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt"))
