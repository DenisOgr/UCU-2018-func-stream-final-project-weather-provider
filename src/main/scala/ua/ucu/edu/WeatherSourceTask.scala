package ua.ucu.edu

import java.time.Instant
import java.util
import java.util.Collections

import cats.effect.IO
import com.snowplowanalytics.weather.Errors.WeatherError
import com.snowplowanalytics.weather.providers.openweather.Responses.Current
import com.snowplowanalytics.weather.providers.openweather.{OpenWeatherMap, OwmClient}
import io.circe.generic.auto._
import io.circe.syntax._
import org.apache.kafka.connect.data.Schema
import org.apache.kafka.connect.source.{SourceRecord, SourceTask}
import ua.ucu.edu.models.Weather

import scala.io.Source

class WeatherSourceTask  extends SourceTask{

  private var client: OwmClient[IO] = _
  private var topic: String = _
  private var data_points: List[Array[String]] = _

  override def start(props: util.Map[String, String]): Unit = {
    this.client = OpenWeatherMap.basicClient[IO](props.get("AppId"))
    this.topic = props.get("KafkaTopic")
    val bufferedSource = Source.fromURL(props.get("DataFile"))
    this.data_points = bufferedSource.getLines().drop(1).map(_.split(",")).toList
  }

  override def poll(): util.List[SourceRecord] = {
    //Restriction from source API(openweathermap.com)
    println("Run poll. I will go to sleep for 10 sec")
    Thread.sleep(10000)

    val sourcePartitionKey = Collections.singletonMap("Partition", 0)

    val records = new util.ArrayList[SourceRecord]
    for (point <- this.data_points) {
      println("Data point: "+ point.mkString(" "))

      //Restriction from source API(openweathermap.com)
      Thread.sleep(1000)

      var message:String = ""

      try {
        message = getWeatherData(point).asJson.noSpaces
      } catch {
        case e:Throwable => {
          println("Error. Message: " + e.getMessage)
        }

      }
      if (!message.isEmpty) {
        records.add(new SourceRecord(
          sourcePartitionKey,
          Collections.singletonMap("Offset", Instant.now().toEpochMilli()),
          this.topic,
          Schema.STRING_SCHEMA,
          point(5).toString,
          Schema.STRING_SCHEMA,
          message
        ))
      } else {
        println("Skip! Message: " + message)
      }
    }
    records
  }

  override def stop(): Unit = {}

  override def version(): String = "v0.1"

  def getWeatherData(point: Array[String]): Weather ={
    val weather: IO[Either[WeatherError, Current]] = this.client.currentByCoords(0.01f+{point(3)+"f"}.toFloat,0.01f+{point(4)+"f"}.toFloat)
    val weatherCurrent = weather.unsafeRunSync().getOrElse().asInstanceOf[Current]
    val message = Weather(
      weatherCurrent.main.temp,
      weatherCurrent.main.pressure,
      weatherCurrent.main.temp_min,
      weatherCurrent.main.temp_max,
      weatherCurrent.wind.speed,
      weatherCurrent.clouds.all,
      weatherCurrent.main.humidity
    )
    message
  }
}
