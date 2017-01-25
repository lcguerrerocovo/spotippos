name := "spotippos"

version := "1.0"

scalaVersion := "2.11.7"

resolvers ++= Seq(
  "twttr" at "https://maven.twttr.com/",
  "Artima Maven Repository" at "http://repo.artima.com/releases"
)

libraryDependencies  ++= Seq(
  "com.github.finagle" % "finch-core_2.11" % "0.12.0",
  "com.twitter" %% "twitter-server" % "1.26.0",
  "com.github.finagle" %% "finch-circe" % "0.12.0",
  "io.circe" %% "circe-generic" % "0.7.0",
  "io.circe" %% "circe-parser" % "0.7.0",
  "com.typesafe.akka" %% "akka-actor" % "2.3.4",
  "org.scalactic" %% "scalactic" % "3.0.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.scalacheck" %% "scalacheck" % "1.13.4" % "test"
)

// for debugging sbt problems
logLevel := Level.Debug

scalacOptions += "-deprecation"