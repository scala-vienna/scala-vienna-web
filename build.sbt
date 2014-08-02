name := "scala-vienna-web"

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .enablePlugins(SbtTwirl)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
    cache,
    ws,
    "org.webjars" %% "webjars-play" % "2.3.0",
    "org.webjars" % "bootstrap" % "3.0.2",
    "org.webjars" % "holderjs" % "2.1.0",
    "rome" % "rome" % "1.0",
    "org.pegdown" % "pegdown" % "1.4.2"
)

scalacOptions ++= Seq("-feature")

scalariformSettings

// sbteclipse settings
EclipseKeys.executionEnvironment := Some(EclipseExecutionEnvironment.JavaSE17)

EclipseKeys.withSource := true