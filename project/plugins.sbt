// Copyright (C) 2009-2019 Lightbend Inc. <https://www.lightbend.com>

enablePlugins(BuildInfoPlugin)

// when updating sbtNativePackager version, be sure to also update the documentation links in
// documentation/manual/working/commonGuide/production/Deploying.md
val sbtNativePackager  = "1.5.2"
val mima               = "0.6.1"
val sbtJavaAgent       = "0.1.5"
val sbtJavaFormatter   = "0.4.4"
val sbtJmh             = "0.3.7"
val webjarsLocatorCore = "0.43"
val sbtHeader          = "5.2.0"
val scalafmt           = "2.0.1"
val sbtTwirl: String   = sys.props.getOrElse("twirl.version", "1.5.0") // sync with documentation/project/plugins.sbt
val interplay: String  = sys.props.getOrElse("interplay.version", "2.1.4")

buildInfoKeys := Seq[BuildInfoKey](
  "sbtNativePackagerVersion" -> sbtNativePackager,
  "sbtTwirlVersion"          -> sbtTwirl,
  "sbtJavaAgentVersion"      -> sbtJavaAgent
)

logLevel := Level.Warn

scalacOptions ++= Seq("-deprecation", "-language:_")

addSbtPlugin("com.typesafe.play"  % "interplay"          % interplay)
addSbtPlugin("com.typesafe.sbt"   % "sbt-twirl"          % sbtTwirl)
addSbtPlugin("com.typesafe"       % "sbt-mima-plugin"    % mima)
addSbtPlugin("com.lightbend.sbt"  % "sbt-javaagent"      % sbtJavaAgent)
addSbtPlugin("com.lightbend.sbt"  % "sbt-java-formatter" % sbtJavaFormatter)
addSbtPlugin("pl.project13.scala" % "sbt-jmh"            % sbtJmh)
addSbtPlugin("de.heikoseeberger"  % "sbt-header"         % sbtHeader)
addSbtPlugin("org.scalameta"      % "sbt-scalafmt"       % scalafmt)

libraryDependencies ++= Seq(
  "org.webjars" % "webjars-locator-core" % webjarsLocatorCore
)

resolvers += Resolver.typesafeRepo("releases")
