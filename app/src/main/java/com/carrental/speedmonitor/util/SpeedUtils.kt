package com.carrental.speedmonitor.util

//Utility function to check if speed is over limit.

object SpeedUtils {
    fun isOverSpeedLimit(currentSpeed: Int, maxSpeed: Int): Boolean {
        return currentSpeed > maxSpeed
    }
}
