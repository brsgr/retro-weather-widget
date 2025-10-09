package com.example.weatherwidget

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

object LocationService {
    private const val TAG = "LocationService"

    suspend fun getCurrentLocation(context: Context): Coordinates? = withContext(Dispatchers.Main) {
        try {
            // Check for location permissions
            val hasFineLocation = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            val hasCoarseLocation = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasFineLocation && !hasCoarseLocation) {
                Log.e(TAG, "Location permissions not granted")
                return@withContext null
            }

            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            // Check if GPS provider is enabled
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!isGpsEnabled && !isNetworkEnabled) {
                Log.e(TAG, "No location providers enabled")
                return@withContext null
            }

            // Try to get last known location first (faster)
            val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            var bestLocation: Location? = null

            for (provider in providers) {
                try {
                    val location = locationManager.getLastKnownLocation(provider)
                    if (location != null && (bestLocation == null || location.accuracy < bestLocation.accuracy)) {
                        bestLocation = location
                    }
                } catch (e: SecurityException) {
                    Log.e(TAG, "Security exception getting location from $provider", e)
                }
            }

            if (bestLocation != null) {
                Log.d(TAG, "Got location: ${bestLocation.latitude}, ${bestLocation.longitude}")
                return@withContext Coordinates(bestLocation.latitude, bestLocation.longitude)
            } else {
                Log.e(TAG, "No last known location available")
                return@withContext null
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error getting location", e)
            null
        }
    }
}
