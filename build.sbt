name := "play-akka-serial-angle-recording"

version := "1.0-SNAPSHOT"

// package settings
maintainer in Linux := "aknay <aknay@outlook.com>"

packageDescription := "Web-based Angle Recording"

daemonUser in Linux := "%i"

lazy val root = (project in file(".")).enablePlugins(PlayScala, SystemdPlugin)

scalaVersion := "2.11.11"

javaOptions in Universal ++= Seq(
  "-J-Xmx512m",
  "-J-Xms256m",
  s"-Dhttp.port=80"
)

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-Xlint",
  "-Ywarn-unused",
  "-Ywarn-dead-code",
  "-Ywarn-unused-import",
  "-Ywarn-numeric-widen",
  "-feature",
  "-language:_"
)


// scalaz-bintray resolver needed for specs2 library
resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
resolvers += Resolver.jcenterRepo

libraryDependencies += ws

libraryDependencies += "org.webjars" % "flot" % "0.8.3"
libraryDependencies += "org.webjars" % "bootstrap" % "3.3.6"

lazy val akkaVersion = "2.4.18"
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test
libraryDependencies += "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test

libraryDependencies += "ch.jodersky" %% "akka-serial-core" % "4.1.0"
libraryDependencies += "ch.jodersky" % "akka-serial-native" % "4.1.0" % "runtime"
libraryDependencies += "ch.jodersky" %% "akka-serial-stream" % "4.1.0"

libraryDependencies ++= Seq(
  specs2 % Test,
  "com.typesafe.play" %% "play-slick" % "2.1.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "2.1.0",
  "com.typesafe.slick" %% "slick-codegen" % "2.1.0",
  "com.adrianhurt" %% "play-bootstrap" % "1.1-P25-B3",
  "org.postgresql" % "postgresql" % "9.4-1206-jdbc4",
  "net.codingwell" %% "scala-guice" % "4.0.1",
  "com.iheart" %% "ficus" % "1.4.0"
)
