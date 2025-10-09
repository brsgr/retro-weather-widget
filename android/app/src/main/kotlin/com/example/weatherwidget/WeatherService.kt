package com.example.weatherwidget

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class WeatherData(
    val temperature: Double,
    val weatherCode: Int
)

object WeatherService {
    private const val TAG = "WeatherService"

    suspend fun fetchWeather(latitude: Double, longitude: Double): WeatherData? = withContext(Dispatchers.IO) {
        try {
            val urlString = "https://api.open-meteo.com/v1/forecast?" +
                    "latitude=$latitude&longitude=$longitude" +
                    "&current_weather=true&temperature_unit=fahrenheit"

            Log.d(TAG, "Fetching weather from: $urlString")

            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d(TAG, "Weather API response: $response")

                val json = JSONObject(response)
                val currentWeather = json.getJSONObject("current_weather")
                val temperature = currentWeather.getDouble("temperature")
                val weatherCode = currentWeather.getInt("weathercode")

                Log.d(TAG, "Temperature: $temperature°F, Weather code: $weatherCode")

                WeatherData(temperature, weatherCode)
            } else {
                Log.e(TAG, "HTTP error: $responseCode")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching weather", e)
            null
        }
    }

    fun getWeatherDescription(weatherCode: Int): String {
        return when (weatherCode) {
            0 -> "Clear"
            1, 2, 3 -> "Partly Cloudy"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Drizzle"
            61, 63, 65 -> "Rainy"
            71, 73, 75 -> "Snowy"
            77 -> "Snow Grains"
            80, 81, 82 -> "Rain Showers"
            85, 86 -> "Snow Showers"
            95 -> "Thunderstorm"
            96, 99 -> "Thunderstorm with Hail"
            else -> "Unknown"
        }
    }
}
