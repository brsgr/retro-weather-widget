package com.example.weatherwidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class WeatherUpdateWorker(context: Context, params: WorkerParameters) :
        CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "WeatherUpdateWorker started")

        return try {
            // Get all widget IDs
            val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
            val componentName = ComponentName(applicationContext, WeatherWidgetReceiver::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            if (appWidgetIds.isEmpty()) {
                Log.d(TAG, "No widgets to update")
                return Result.success()
            }

            Log.d(TAG, "Updating ${appWidgetIds.size} widget(s)")

            // Get saved location
            val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedLat = prefs.getFloat(PREF_LAST_LAT, Float.NaN)
            val savedLon = prefs.getFloat(PREF_LAST_LON, Float.NaN)

            var location: Coordinates? = null

            // Use saved location if available
            if (!savedLat.isNaN() && !savedLon.isNaN()) {
                location = Coordinates(savedLat.toDouble(), savedLon.toDouble())
                Log.d(TAG, "Using saved location: ${location.latitude}, ${location.longitude}")
            } else {
                // Fall back to GPS location if no saved location
                location = LocationService.getCurrentLocation(applicationContext)
                if (location != null) {
                    Log.d(TAG, "Got GPS location: ${location.latitude}, ${location.longitude}")
                    // Save this GPS location for future use
                    prefs.edit().apply {
                        putFloat(PREF_LAST_LAT, location.latitude.toFloat())
                        putFloat(PREF_LAST_LON, location.longitude.toFloat())
                        apply()
                    }
                }
            }

            if (location == null) {
                Log.e(TAG, "Failed to get location")
                return Result.retry()
            }

            // Fetch weather
            val weatherData = WeatherService.fetchWeather(location.latitude, location.longitude)

            if (weatherData != null) {
                Log.d(
                        TAG,
                        "Weather fetched: ${weatherData.temperature}°F, ${WeatherService.getWeatherDescription(weatherData.weatherCode)}"
                )

                // Trigger widget update via broadcast
                val receiver = WeatherWidgetReceiver()
                receiver.updateAllWidgets(
                        applicationContext,
                        appWidgetManager,
                        appWidgetIds,
                        weatherData,
                        location
                )

                Result.success()
            } else {
                Log.e(TAG, "Failed to fetch weather")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in WeatherUpdateWorker", e)
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "WeatherUpdateWorker"
        private const val PREFS_NAME = "WeatherWidgetPrefs"
        private const val PREF_LAST_LAT = "last_latitude"
        private const val PREF_LAST_LON = "last_longitude"
    }
}
