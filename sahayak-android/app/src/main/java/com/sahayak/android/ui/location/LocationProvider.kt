package com.sahayak.android.ui.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Lightweight wrapper around [FusedLocationProviderClient] that
 * exposes the user's location as a coroutine-friendly API.
 */
class LocationProvider(private val context: Context) {

    private val client: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /** Returns true when EITHER fine or coarse location is granted. */
    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    /**
     * Fetch the last known location (fast, may be null).
     * Falls back to a single fresh fix if last-known is unavailable.
     */
    @Suppress("MissingPermission")
    suspend fun getLastLocation(): Location? {
        if (!hasPermission()) return null
        return suspendCancellableCoroutine { cont ->
            client.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        cont.resume(location)
                    } else {
                        // No cached location → request a single fresh fix
                        requestSingleFix { cont.resume(it) }
                    }
                }
                .addOnFailureListener { cont.resume(null) }
        }
    }

    /**
     * Emit location updates as a [Flow].  Automatically stops
     * when the collector is cancelled.
     */
    @Suppress("MissingPermission")
    fun locationUpdates(
        intervalMs: Long = 10_000,
        priority: Int = Priority.PRIORITY_BALANCED_POWER_ACCURACY,
    ): Flow<Location> = callbackFlow {
        if (!hasPermission()) {
            close()
            return@callbackFlow
        }
        val request = LocationRequest.Builder(priority, intervalMs)
            .setMinUpdateIntervalMillis(intervalMs / 2)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(it) }
            }
        }
        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
        awaitClose { client.removeLocationUpdates(callback) }
    }

    // ── Internal ─────────────────────────────

    @Suppress("MissingPermission")
    private fun requestSingleFix(onResult: (Location?) -> Unit) {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 1_000
        ).setMaxUpdates(1).build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                client.removeLocationUpdates(this)
                onResult(result.lastLocation)
            }
        }
        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
    }
}
