package com.example.weatherwidget

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : FlutterActivity() {

    private val CHANNEL = "com.example.weatherwidget/location"
    private val PREFS_NAME = "WeatherWidgetPrefs"
    private val PREF_LAST_LAT = "last_latitude"
    private val PREF_LAST_LON = "last_longitude"

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
                call,
                result ->
            when (call.method) {
                "getLocation" -> {
                    val location = getSavedLocation()
                    if (location != null) {
                        result.success(
                                mapOf(
                                        "latitude" to location.latitude,
                                        "longitude" to location.longitude
                                )
                        )
                    } else {
                        result.success(null)
                    }
                }
                "refreshLocation" -> {
                    CoroutineScope(Dispatchers.Main).launch {
                        val location = LocationService.getCurrentLocation(applicationContext)
                        if (location != null) {
                            saveLocation(location)
                            updateWidget()
                            result.success(
                                    mapOf(
                                            "latitude" to location.latitude,
                                            "longitude" to location.longitude
                                    )
                            )
                        } else {
                            result.error("LOCATION_ERROR", "Failed to get location", null)
                        }
                    }
                }
                "setLocation" -> {
                    val latitude = call.argument<Double>("latitude")
                    val longitude = call.argument<Double>("longitude")

                    if (latitude != null && longitude != null) {
                        val location = Coordinates(latitude, longitude)
                        saveLocation(location)
                        updateWidget()
                        result.success(
                                mapOf(
                                        "latitude" to location.latitude,
                                        "longitude" to location.longitude
                                )
                        )
                    } else {
                        result.error("INVALID_ARGS", "Latitude and longitude are required", null)
                    }
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request location permissions on app start
        requestLocationPermissions()
    }

    private fun requestLocationPermissions() {
        val hasFineLoc =
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED

        val hasCoarseLoc =
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

        if (!hasFineLoc || !hasCoarseLoc) {
            // Request both permissions
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            val granted =
                    grantResults.isNotEmpty() &&
                            grantResults.all { it == PackageManager.PERMISSION_GRANTED }

            if (granted) {
                // Permissions granted - trigger widget update
                android.util.Log.d("MainActivity", "Location permissions granted")
            } else {
                android.util.Log.w("MainActivity", "Location permissions denied")
            }
        }
    }

    private fun getSavedLocation(): Coordinates? {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lat = prefs.getFloat(PREF_LAST_LAT, Float.NaN)
        val lon = prefs.getFloat(PREF_LAST_LON, Float.NaN)

        return if (!lat.isNaN() && !lon.isNaN()) {
            Coordinates(lat.toDouble(), lon.toDouble())
        } else {
            null
        }
    }

    private fun saveLocation(location: Coordinates) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putFloat(PREF_LAST_LAT, location.latitude.toFloat())
            putFloat(PREF_LAST_LON, location.longitude.toFloat())
            apply()
        }
    }

    private fun updateWidget() {
        val intent =
                Intent(this, WeatherWidgetReceiver::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                }
        val ids =
                AppWidgetManager.getInstance(application)
                        .getAppWidgetIds(
                                ComponentName(application, WeatherWidgetReceiver::class.java)
                        )
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)
    }
}
