package app

import cats.effect.IO
import com.snowplowanalytics.weather.Errors.WeatherError
import com.snowplowanalytics.weather.providers.openweather.OpenWeatherMap
import com.snowplowanalytics.weather.providers.openweather.Responses.Current
import io.circe.Json

import scala.io.Source

object Main extends App {
    val client = OpenWeatherMap.basicClient[IO]("d84042cd1f143195437a8195d45cf0c4")

  val bufferedSource = Source.fromURL("https://gist.githubusercontent.com/DenisOgr/998438838197c66ce1966bc3ffdb2163/raw/9e118545b168df9b07e121f4c1d7a49060669b9a/data_v2.csv")
  val data_points = bufferedSource.getLines().drop(1).map(_.split(",")).toList

    val point = data_points(4)
    println("Get data from API for: "+ point(1).toString)
    println("Get data 2: "+ point(2).toString)
    println("Get data 3: "+ point(3).toString)
    println(Json.fromFields(getWeatherData(point)).toString())

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


