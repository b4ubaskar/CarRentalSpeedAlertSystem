package com.carrental.speedmonitor.service

import android.util.Log
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.sns.AmazonSNSClient
import com.amazonaws.services.sns.model.PublishRequest

class AwsSnsNotifier {

    private val accessKey = "AWS_ACCESS_KEY"
    private val secretKey = "AWS_SECRET_KEY"
    private val topicArn = "TOPIC_ARN" // or use phone number/email directly

    private val credentials = BasicAWSCredentials(accessKey, secretKey)
    private val snsClient = AmazonSNSClient(credentials).apply {
        setRegion(com.amazonaws.regions.Region.getRegion(Regions.US_EAST_1)) // Change region as needed
    }

    fun sendSms(message: String, phoneNumber: String) {
        try {
            val request = PublishRequest()
                .withMessage(message)
                .withPhoneNumber(phoneNumber)
            snsClient.publish(request)
            Log.d("AWS SNS", "SMS sent to $phoneNumber")
        } catch (e: Exception) {
            Log.e("AWS SNS", "Failed to send SMS", e)
        }
    }

    fun sendEmail(message: String) {
        try {
            val request = PublishRequest()
                .withMessage(message)
                .withTopicArn(topicArn)
            snsClient.publish(request)
            Log.d("AWS SNS", "Email alert published to topic")
        } catch (e: Exception) {
            Log.e("AWS SNS", "Failed to send email", e)
        }
    }
}
