package com.example.weatherwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.util.Log
import android.widget.RemoteViews
import android.app.PendingIntent
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WeatherWidgetReceiver : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "onUpdate called for ${appWidgetIds.size} widgets")

        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_WIDGET_CLICKED) {
            Log.d(TAG, "Widget was clicked! Fetching location and weather...")

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                intent.component
            )

            // Fetch location and weather data
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

                val weatherData = WeatherService.fetchWeather(location.latitude, location.longitude)

                if (weatherData != null) {
                    Log.d(TAG, "Weather fetched: ${weatherData.temperature}°F, ${WeatherService.getWeatherDescription(weatherData.weatherCode)}")

                    // Update all widgets
                    for (appWidgetId in appWidgetIds) {
                        updateWidgetWithWeather(context, appWidgetManager, appWidgetId, weatherData)
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
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.weather_widget)

        // Set up click listener
        val intent = Intent(context, WeatherWidgetReceiver::class.java).apply {
            action = ACTION_WIDGET_CLICKED
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun updateWidgetWithWeather(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        weatherData: WeatherData
    ) {
        val views = RemoteViews(context.packageName, R.layout.weather_widget)

        // Update temperature and conditions
        views.setTextViewText(R.id.temperature_text, "${weatherData.temperature.toInt()}°F")
        views.setTextViewText(R.id.conditions_text, WeatherService.getWeatherDescription(weatherData.weatherCode))

        // Set up click listener
        val intent = Intent(context, WeatherWidgetReceiver::class.java).apply {
            action = ACTION_WIDGET_CLICKED
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
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

        views.setTextViewText(R.id.temperature_text, "Error")
        views.setTextViewText(R.id.conditions_text, errorMessage)

        // Set up click listener
        val intent = Intent(context, WeatherWidgetReceiver::class.java).apply {
            action = ACTION_WIDGET_CLICKED
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        private const val TAG = "WeatherWidget"
        private const val ACTION_WIDGET_CLICKED = "com.example.weatherwidget.WIDGET_CLICKED"
    }
}
