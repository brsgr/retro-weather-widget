package com.example.weatherwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.util.Log
import android.widget.RemoteViews
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WeatherWidgetReceiver : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "onUpdate called for ${appWidgetIds.size} widgets - fetching weather automatically")

        // Fetch weather for all widgets automatically
        fetchAndUpdateWeather(context, appWidgetManager, appWidgetIds)
    }

    private fun fetchAndUpdateWeather(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            val location = LocationService.getCurrentLocation(context)

            if (location == null) {
                Log.e(TAG, "Failed to get location")
                // Update widgets with error message
                for (appWidgetId in appWidgetIds) {
                    updateWidgetWithError(context, appWidgetManager, appWidgetId, "No location")
                }
                return@launch
            }

            Log.d(TAG, "Got location: ${location.latitude}, ${location.longitude}")

            // Save location for opening Google Weather later
            saveLocation(context, location)

            val weatherData = WeatherService.fetchWeather(location.latitude, location.longitude)

            if (weatherData != null) {
                Log.d(TAG, "Weather fetched: ${weatherData.temperature}°F, ${WeatherService.getWeatherDescription(weatherData.weatherCode)}")

                // Update all widgets
                for (appWidgetId in appWidgetIds) {
                    updateWidgetWithWeather(context, appWidgetManager, appWidgetId, weatherData, location)
                }
            } else {
                Log.e(TAG, "Failed to fetch weather")
                // Update widgets with error message
                for (appWidgetId in appWidgetIds) {
                    updateWidgetWithError(context, appWidgetManager, appWidgetId, "Weather error")
                }
            }
        }
    }

    private fun updateWidgetWithWeather(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        weatherData: WeatherData,
        location: Coordinates
    ) {
        val views = RemoteViews(context.packageName, R.layout.weather_widget)

        // Create bitmaps with custom font
        val tempText = "${weatherData.temperature.toInt()}°F"
        val conditionsText = WeatherService.getWeatherDescription(weatherData.weatherCode)

        val tempBitmap = TextBitmapHelper.createTextBitmap(
            context,
            tempText,
            72f, // 24sp * 3 (approx dp to px)
            android.graphics.Color.WHITE
        )

        val conditionsBitmap = TextBitmapHelper.createTextBitmap(
            context,
            conditionsText,
            36f, // 12sp * 3 (approx dp to px)
            android.graphics.Color.WHITE
        )

        views.setImageViewBitmap(R.id.temperature_text, tempBitmap)
        views.setImageViewBitmap(R.id.conditions_text, conditionsBitmap)

        // Set up click listener to open Google Weather
        val pendingIntent = createGoogleWeatherIntent(context, location)
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun updateWidgetWithError(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        errorMessage: String
    ) {
        val views = RemoteViews(context.packageName, R.layout.weather_widget)

        // Create error bitmaps with custom font
        val errorBitmap = TextBitmapHelper.createTextBitmap(
            context,
            "Error",
            72f,
            android.graphics.Color.WHITE
        )

        val messageBitmap = TextBitmapHelper.createTextBitmap(
            context,
            errorMessage,
            36f,
            android.graphics.Color.WHITE
        )

        views.setImageViewBitmap(R.id.temperature_text, errorBitmap)
        views.setImageViewBitmap(R.id.conditions_text, messageBitmap)

        // Try to get saved location, or use default
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lat = prefs.getFloat(PREF_LAST_LAT, 0f).toDouble()
        val lon = prefs.getFloat(PREF_LAST_LON, 0f).toDouble()

        val location = if (lat != 0.0 && lon != 0.0) {
            Coordinates(lat, lon)
        } else {
            null
        }

        val pendingIntent = if (location != null) {
            createGoogleWeatherIntent(context, location)
        } else {
            createGoogleWeatherIntent(context, null)
        }

        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun createGoogleWeatherIntent(context: Context, location: Coordinates?): PendingIntent {
        // Try to open Google Weather app
        val intent = Intent().apply {
            component = ComponentName(
                "com.google.android.googlequicksearchbox",
                "com.google.android.apps.search.weather.WeatherExportedActivity"
            )
            flags = Intent.FLAG_ACTIVITY_NEW_TASK

            // If we have location, try to pass it (though Google Weather may not use it)
            location?.let {
                putExtra("latitude", it.latitude)
                putExtra("longitude", it.longitude)
            }
        }

        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun saveLocation(context: Context, location: Coordinates) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putFloat(PREF_LAST_LAT, location.latitude.toFloat())
            putFloat(PREF_LAST_LON, location.longitude.toFloat())
            apply()
        }
    }

    companion object {
        private const val TAG = "WeatherWidget"
        private const val PREFS_NAME = "WeatherWidgetPrefs"
        private const val PREF_LAST_LAT = "last_latitude"
        private const val PREF_LAST_LON = "last_longitude"
    }
}
