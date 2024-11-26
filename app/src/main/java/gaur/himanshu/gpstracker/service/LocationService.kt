package gaur.himanshu.gpstracker.service

import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import gaur.himanshu.gpstracker.CHANNEL_ID
import gaur.himanshu.gpstracker.R
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



class LocationService : Service() {
    private val client = OkHttpClient();

    private val fusedLocationProviderClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(
            this
        )
    }
    private val locationRequest: LocationRequest by lazy {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 30000)
            .apply {
                setIntervalMillis(30000)
            }.build()
    }
    private val locationCallback: LocationCallback by lazy {
        object : LocationCallback() {
            override fun onLocationAvailability(p0: LocationAvailability) {
                super.onLocationAvailability(p0)
            }

            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation!!
                createNotification(location.latitude.toString(), location.longitude.toString())
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        locationUpdates()
        return START_STICKY
    }

    private fun locationUpdates() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    fusedLocationProviderClient.requestLocationUpdates(
                            locationRequest,
                            locationCallback,
                            Looper.getMainLooper()
                        )
                }
            } else {
                fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    null
                )
            }
        }
    }

    @Suppress("MissingPermission")
    fun createNotification(lat: String, lng: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Latitude and Longitude")
            .setContentText("$lat - $lng")
            .build()
        startForeground(1, notification)
//
//         val mediaType = "application/json".toMediaType()
//        val body = "{\"latitude\":lat,\n    \"longitude\":lng}".toRequestBody(mediaType)
//        val request = Request.Builder()
//                .url("https://webhook.site/01dda925-a648-4d4c-9afc-7d8d676aa0ef")
//                .post(body)
//                .addHeader("Content-Type", "application/json")
//                .build()
//        val response = client.newCall(request).execute()

//        try {
//            val mediaType = "application/json".toMediaType()
//            val body = """
//            {
//                "latitude": "$lat",
//                "longitude": "$lng"
//            }
//        """.trimIndent().toRequestBody(mediaType)
//            Log.d("LocationService", "Latitude: $lat, Longitude: $lng, body:$body")
//            val request = Request.Builder()
//                .url("https://webhook.site/01dda925-a648-4d4c-9afc-7d8d676aa0ef")
//                .post(body)
//                .addHeader("Content-Type", "application/json")
//                .build()
//
//            Log.d("LocationService", "Sending request: $body")
//
//            val response = client.newCall(request).execute()
//
//            if (response.isSuccessful) {
//                Log.d("LocationService", "Request successful: ${response.body?.string()}")
//            } else {
//                Log.e("LocationService", "Request failed with code: ${response.code}")
//            }
//
//        } catch (e: Exception) {
//            Log.e("LocationService", "Error making API call", e)
//            Log.e("LocationService", "Error making API call: ${e.message}")
//            e.printStackTrace()
//            Log.e("LocationService", "Stack Trace: ${Log.getStackTraceString(e)}")
//        }


        CoroutineScope(Dispatchers.IO).launch {
            try {
                val mediaType = "application/json".toMediaType()
                val body = """
            {
                "latitude": "$lat",
                "longitude": "$lng"
            }
        """.trimIndent().toRequestBody(mediaType)

                val request = Request.Builder()
                    .url("https://webhook.site/c3d5adcc-5ffe-40bd-bd6d-7707ca008efa")
//                    .url("https://webhook-test.com/d0e12183b17078ca2a568e8b4555aaf6")
//                    .url("https://famous-portugal-81.webhook.cool")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build()

                val response = client.newCall(request).execute()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Log.d("LocationService", "Request successful: ${response.body?.string()}")
                    } else {
                        Log.e("LocationService", "Request failed with code: ${response.code}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("LocationService", "Error making API call", e)
                    Log.e("LocationService", "Error details: ${e.message}")
                    e.printStackTrace()
                }
            }
        }

    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

}