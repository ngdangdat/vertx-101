
name := "vertx-scala-101"

version := "0.1"

scalaVersion := "2.12.10"

name := "vertx-martiply-api"

version := "0.3.3"

lazy val vertxVersion = "3.5.4"

mainClass in assembly := Some("com.vertx.api.Application")

resolvers += "jcenter" at "https://jcenter.bintray.com/"
resolvers += "jitpack" at "https://jitpack.io"

scalacOptions := Seq("-unchecked", "-deprecation")
scalacOptions += "-feature"


libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" % Test,
  "org.mockito" % "mockito-core" % "2.19.1" % Test
)

libraryDependencies += "io.vertx" %% "vertx-lang-scala" % vertxVersion
libraryDependencies += "io.vertx" %% "vertx-config-scala" % vertxVersion
libraryDependencies += "io.vertx" %% "vertx-web-scala" % vertxVersion

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case PathList("META-INF", xs@_*) => MergeStrategy.last
  case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.first
  case PathList("codegen.json") => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

// libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.25"
