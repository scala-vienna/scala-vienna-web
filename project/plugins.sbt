// Comment to get more information during initialization
//logLevel := Level.Warn

// The Typesafe repository 
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.2.0")

addSbtPlugin("com.jamesward" %% "play-auto-refresh" % "0.0.10")

// Eclipse project generator
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.5.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-twirl" % "1.0.2")