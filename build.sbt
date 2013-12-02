name := "scala-vienna-web"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
    "org.webjars" %% "webjars-play" % "2.2.1",
    "org.webjars" % "bootstrap" % "3.0.2",
    "org.webjars" % "holderjs" % "2.1.0",
    "rome" % "rome" % "1.0"
)

play.Project.playScalaSettings

scalariformSettings

com.jamesward.play.BrowserNotifierPlugin.livereload


