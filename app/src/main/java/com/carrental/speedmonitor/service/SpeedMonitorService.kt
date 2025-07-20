package com.carrental.speedmonitor.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import com.carrental.speedmonitor.R
import com.carrental.speedmonitor.data.RentalManager
import com.carrental.speedmonitor.util.SpeedUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlin.math.roundToInt
import android.car.Car
import android.car.hardware.property.CarPropertyManager
import android.car.hardware.CarPropertyValue


//Background service that checks speed and triggers alerts.

class SpeedMonitorService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var snsNotifier: AwsSnsNotifier

    private var useVehicleSpeed = false
    private lateinit var car: Car
    private lateinit var propertyManager: CarPropertyManager


    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onCreate() {
        super.onCreate()

        snsNotifier = AwsSnsNotifier()
        startForegroundNotification()

        try {
            car = Car.createCar(this)
            car.connect()
            car.addCarServiceLifecycleListener(object : Car.CarServiceLifecycleListener {
                @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
                override fun onLifecycleChanged(car: Car, ready: Boolean) {
                    if (ready) {
                        try {
                            propertyManager = car.getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager
                            propertyManager.registerCallback(
                                speedCallback,
                                VehiclePropertyIds.PERF_VEHICLE_SPEED,
                                CarPropertyManager.SENSOR_RATE_NORMAL
                            )
                            useVehicleSpeed = true
                        } catch (e: Exception) {
                            e.printStackTrace()
                            useVehicleSpeed = false
                            startGpsSpeedTracking()
                        }
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            useVehicleSpeed = false
            startGpsSpeedTracking()
        }
    }

    // === Vehicle speed callback ===
    private val speedCallback = object : CarPropertyEventCallback {
        override fun onChangeEvent(value: CarPropertyValue<*>) {
            val speedKmph = (value.value as Float).toInt()
            checkSpeedAndNotify(speedKmph)
        }

        override fun onErrorEvent(propId: Int, areaId: Int) {
            android.util.Log.e("SpeedMonitorService", "CarProperty error: propId=$propId")
        }
    }

    // GPS speed fallback
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun startGpsSpeedTracking() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 3000L
        )
            .setMinUpdateIntervalMillis(2000L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                val speedKmph = (location.speed * 3.6).roundToInt()
                checkSpeedAndNotify(speedKmph)
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.getMainLooper()
        )
    }

    private fun checkSpeedAndNotify(speedKmph: Int) {
        val customer = RentalManager.getCurrentCustomer()
        customer?.let {
            if (SpeedUtils.isOverSpeedLimit(speedKmph, it.maxSpeed)) {
                showWarning(speedKmph)

                // Notify via Firebase
                FirebaseNotifier.notifyFleet(it.id, speedKmph)


                // Notify via AWS SNS
                val alertMessage = "Speed Violation Detected: $speedKmph km/h (limit: ${it.maxSpeed})"
                snsNotifier.sendSms(alertMessage, "+919087654321") // Use real number
                snsNotifier.sendEmail(alertMessage)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::propertyManager.isInitialized && useVehicleSpeed) {
            propertyManager.unregisterCallback(speedCallback)
        }
        if (::fusedLocationClient.isInitialized && !useVehicleSpeed) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
        if (::car.isInitialized) {
            car.disconnect()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun showWarning(speed: Int) {
        Toast.makeText(
            this,
            "⚠️ Speed exceeded: $speed km/h",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun startForegroundNotification() {
        val channelId = "speed_monitor_channel"
        val channelName = "Speed Monitor Service"

        val channel = NotificationChannel(
            channelId, channelName, NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Speed Monitoring Active")
            .setContentText("Tracking vehicle speed...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }
}

