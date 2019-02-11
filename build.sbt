name := "UCU-2018-scala-final-project-weather-provider(Kafka connect)"

version := "1.2"

scalaVersion := "2.12.8"
val kafkaGroupId = "org.apache.kafka"
val kafkaClientArtifactId = "kafka-clients"
val kafkaClientRevision = "0.10.1.0"
libraryDependencies +=  kafkaGroupId % kafkaClientArtifactId % kafkaClientRevision

val kafkaConnectArtifactId = "connect-api"
libraryDependencies +=  kafkaGroupId % kafkaConnectArtifactId % kafkaClientRevision

libraryDependencies += "com.snowplowanalytics" %% "scala-weather" % "0.4.0"

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
