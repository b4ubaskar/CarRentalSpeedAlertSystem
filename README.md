# SpeedMonitor

## Setup & Configuration

### 1. Clone the Repository

git clone https://github.com/b4ubaskar/CarRentalSpeedAlertSystem.git
cd SpeedMonitor

### 2. Open in Android Studio
- Requires Android Studio Hedgehog or newer

-AAOS emulator or real device recommended

###  Firebase Setup

- Go to Firebase Console

- Create a new project

- Add your app’s package name (e.g., com.carrental.speedmonitor)

- Download google-services.json

-Place it in the app/ directory

- Ensure you have Firebase Cloud Messaging enabled in your Firebase project.

### AWS SNS (Optional)
- To enable AWS SNS integration:

- Create an IAM user with SNS permissions

- Save your credentials in a secure way (use environment variables or encrypted config)

- Configure AwsSnsNotifier with your Topic ARN and region


###  Permissions Required To function properly, the app needs:-

- <uses-permission android:name="android.permission.INTERNET" />
- <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
- <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
- <uses-permission android:name="android.permission.CAR_VENDOR_EXTENSION" /> 
- <uses-feature android:name="android.hardware.type.automotive" />
- <uses-permission android:name="android.car.permission.CAR_SPEED" />

### Dependencies

- implementation(libs.firebase.firestore.ktx)
- implementation(libs.aws.android.sdk.core)
- implementation(libs.aws.android.sdk.sns)
- implementation(libs.play.services.location)

  // For AAOS
- implementation(libs.androidx.car)     // CarPropertyManager
- implementation(libs.androidx.app) // For Android Auto apps
- implementation(libs.car.lib) // For Android Automotive OS (AAOS)

###  Testing the App
- Start a rental session with a customer-specific speed limit

- Inject mock or real GPS speed data (e.g., 80 km/h)

- If the speed exceeds the limit:

- Renter is alerted via Toast or Notification

- Firebase or AWS SNS sends a message to the company

-In real implementation, you'd pull speed data via GPS or AAOS Vehicle APIs.

### Updated Plan for Real Device Testing (AAOS):
We'll modify SpeedMonitorService to:

Check if Car and CarPropertyManager are available.

If yes, use PERF_VEHICLE_SPEED.

Else, fallback to GPS-based speed (optional fallback).

### How to Use
- Start the app

- Click "Rental Start"

- Car starts monitoring speed

- If speed exceeds the limit:

- User gets a warning (Toast)

- Firebase logs the event

- AWS sends SMS/email to fleet manager

- Click "Rental Stop" to stop monitoring


### Testing Notes
- Ensure emulator supports AAOS

- AWS credentials should be valid

- Firebase project must be active with FCM enabled


**Smart Speed Monitoring System for Car Rentals**

SpeedSense is a Kotlin-based Android Automotive OS (AAOS) application designed for car rental
companies to monitor renter driving behavior in real-time. If a vehicle exceeds the permitted speed
limit (defined per customer), the system alerts the renter and notifies the fleet company using
Firebase, with optional AWS SNS integration.

---

## Use Case

A car rental company wants to be alerted if a renter drives at a speed above a predefined limit.
Each customer can have their own speed limit, set before the rental begins. If the speed is
exceeded:

- ✅ The **renter receives an in-car warning**
- ✅ The **rental company is notified** via Firebase Cloud Messaging (FCM)
- 🟡 Optionally, **AWS SNS** can be used to send email or SMS alerts

---

## 🚀 Features Implemented

- Foreground service to continuously monitor speed
- Firebase integration to log and notify speed violations
- AWS SNS integration to send SMS and email alerts
- CarPropertyManager integration to read real-time vehicle speed (if available)
- Fallback to GPS-based speed tracking for compatibility
 
## Tech Stack

- Kotlin
- Android Automotive OS (AAOS)
- Firebase Cloud Messaging
- AWS Simple Notification Service (SNS)
- Google Play Services Location (FusedLocationProviderClient)

```bash
