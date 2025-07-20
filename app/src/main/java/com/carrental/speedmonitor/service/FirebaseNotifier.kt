package com.carrental.speedmonitor.service

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

//Sends alerts to Firebase Firestore when speed is exceeded.

object FirebaseNotifier {

    @SuppressLint("StaticFieldLeak")
    private val firestore = FirebaseFirestore.getInstance()

    fun notifyFleet(customerId: String, speed: Int) {
        val message = hashMapOf(
            "customerId" to customerId,
            "speed" to speed,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("speed_alerts")
            .add(message)
            .addOnSuccessListener {
                Log.d("FirebaseNotifier", "Notification sent.")
            }
            .addOnFailureListener {
                Log.e("FirebaseNotifier", "Failed to notify fleet.", it)
            }
    }
}