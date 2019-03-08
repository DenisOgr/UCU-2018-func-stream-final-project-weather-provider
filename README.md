# UCU-2018-scala-final-project-weather-provider
This is a solar panel sensor provider(Kafka-connect) for scala  final project.
Your task build jar connect. To do this:

Build Docker image (first time only)
```
make build
```
Build fat jar (run docker container and  build jar inside)
```
make assembly
```
Copy `{root}/kafka-connect-weather-panel.jar` to kafka plugins