name := "scala-vienna-web"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
    "org.webjars" %% "webjars-play" % "2.2.1",
    "org.webjars" % "bootstrap" % "3.0.2"
)

play.Project.playScalaSettings

scalariformSettings

com.jamesward.play.BrowserNotifierPlugin.livereload


