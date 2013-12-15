name := "play-currency-rates"

version := "1.0-SNAPSHOT"

resolvers += "Spy Repository" at "http://files.couchbase.com/maven2" // required to resolve `spymemcached`, the plugin's dependency.

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "com.github.mumoshu" %% "play2-memcached" % "0.3.0.2"
)

play.Project.playScalaSettings

