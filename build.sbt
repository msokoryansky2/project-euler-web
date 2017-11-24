name := """project-euler-web"""
organization := "mike.sokoryansky"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.3"

libraryDependencies += guice
libraryDependencies += ws
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "mike.sokoryansky.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "mike.sokoryansky.binders._"

libraryDependencies += "org.webjars" % "flot" % "0.8.3"
libraryDependencies += "org.webjars" % "bootstrap" % "3.3.6"

libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.5.3" % Test
libraryDependencies += "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.3" % Test
// libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % Test
libraryDependencies += "org.awaitility" % "awaitility" % "3.0.0" % Test

libraryDependencies += "com.typesafe.akka" % "akka-actor_2.12" % "2.5.4"

// https://mvnrepository.com/artifact/com.amazonaws/aws-java-sdk-lambda
libraryDependencies += "com.amazonaws" % "aws-java-sdk-lambda" % "1.11.235"
