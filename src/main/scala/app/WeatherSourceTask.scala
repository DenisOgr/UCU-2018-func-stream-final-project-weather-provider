package weatherKafkaConnect

import java.time.Instant
import java.util
import java.util.Collections

import cats.effect.IO
import com.snowplowanalytics.weather.Errors.WeatherError
import com.snowplowanalytics.weather.providers.openweather.Responses.Current
import com.snowplowanalytics.weather.providers.openweather.{OpenWeatherMap, OwmClient}
import io.circe.Json
import org.apache.kafka.connect.data.Schema
import org.apache.kafka.connect.source.{SourceRecord, SourceTask}

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
    println("Run poll. I go to sleep for 10 sec")
    Thread.sleep(10000)
    println("I begin to work")
    val sourcePartitionKey = Collections.singletonMap("Partition", 0)

    val records = new util.ArrayList[SourceRecord]
    for (point <- this.data_points) {
      println("Get data from API for (1): "+ point(1).toString)
      println("Get data from API for (2): "+ point(2).toString)
      println("Get data from API for (3): "+ point(3).toString)
      println("version3")
      Thread.sleep(1000)

      var message:String = ""

      try {
        message = Json.fromFields(getWeatherData(point)).toString()
        println("Finished!")
      } catch {
        case e:Throwable => {
          println("ERROR! Skip it. Message: " + e.getMessage)
          println("ERROR! Skip it. ST: " + e.getStackTrace)
        }
        case e:Exception => {
          println("ERROR!! Skip it. Message: " + e.getMessage)
        }
        case _ => {
          println("ERROR! Skip it.")
        }

      }
      if (!message.isEmpty) {
        records.add(new SourceRecord(
          sourcePartitionKey,
          Collections.singletonMap("Offset", Instant.now().toEpochMilli()),
          this.topic,
          Schema.STRING_SCHEMA,
          "dummyKey",
          Schema.STRING_SCHEMA,
          message
        ))
      } else {
        println("Skip!")
        println("Message: " + message)
      }
    }
    records
  }

  override def stop(): Unit = {}

  override def version(): String = "v0.1"

  def getWeatherData(point: Array[String]): List[(String,Json)] ={
    val weather: IO[Either[WeatherError, Current]] = this.client.currentByCoords(0.01f+{point(2)+"f"}.toFloat,0.01f+{point(3)+"f"}.toFloat)
    val weatherCurrent = weather.unsafeRunSync().getOrElse().asInstanceOf[Current]
    val key = weatherCurrent.coord.get.lon +"_"+ weatherCurrent.coord.get.lat
    val message = List(
      ("temp", Json.fromBigDecimal(weatherCurrent.main.temp)),
      ("pressure", Json.fromBigDecimal(weatherCurrent.main.pressure)),
      ("temp_min", Json.fromBigDecimal(weatherCurrent.main.temp_min)),
      ("temp_max", Json.fromBigDecimal(weatherCurrent.main.temp_max)),
      ("wind_speed", Json.fromBigDecimal(weatherCurrent.wind.speed)),
      ("clouds", Json.fromBigInt(weatherCurrent.clouds.all)),
      ("humidity", Json.fromBigDecimal(weatherCurrent.main.humidity)))
    message
  }
}
