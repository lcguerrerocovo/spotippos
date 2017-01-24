name := "spotippos"

version := "1.0"

scalaVersion := "2.11.7"

resolvers += "twttr" at "https://maven.twttr.com/"

libraryDependencies += "com.github.finagle" % "finch-core_2.11" % "0.12.0"

libraryDependencies += "com.twitter" %% "twitter-server" % "1.26.0"

libraryDependencies += "com.github.finagle" %% "finch-circe" % "0.12.0"

libraryDependencies += "io.circe" %% "circe-generic" % "0.7.0"

libraryDependencies += "io.circe" %% "circe-parser" % "0.7.0"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.4"

// for debugging sbt problems
logLevel := Level.Debug

scalacOptions += "-deprecation"