package com.carrental.speedmonitor.data

import com.carrental.speedmonitor.model.Customer
import com.carrental.speedmonitor.service.AwsSnsNotifier

// This file manages which customer is currently renting a car.

object RentalManager {
    private var currentCustomer: Customer? = null
    private val snsNotifier = AwsSnsNotifier()

    fun startRental(customer: Customer) { //sets the current customer
        currentCustomer = customer

        // Notify AWS SNS
        val message = "Rental started for customer: ${customer.name}, Max Speed: ${customer.maxSpeed} km/h"
        snsNotifier.sendEmail(message)
        snsNotifier.sendSms(message, "+919087654321") // Use actual fleet manager number
    }

    fun getCurrentCustomer(): Customer? = currentCustomer

    fun stopRental() {

        currentCustomer?.let {
            // OPTIONAL: Notify AWS SNS
            val message = "Rental ended for customer: ${it.name}"
            snsNotifier.sendEmail(message)
            snsNotifier.sendSms(message, "+919087654321")
        }

        currentCustomer = null
    }
}