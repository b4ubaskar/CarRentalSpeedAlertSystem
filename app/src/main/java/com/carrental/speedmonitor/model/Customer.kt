package com.carrental.speedmonitor.model

//Data class represents renter data (ID, name, speed limit).

data class Customer(val id: String,
                    val name: String,
                    val maxSpeed: Int) // in km/h
