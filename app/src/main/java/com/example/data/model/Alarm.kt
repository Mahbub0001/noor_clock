package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hour: Int,                // 0-23
    val minute: Int,              // 0-59
    val label: String,            // e.g. "Wake Up for Tahajjud"
    val daysOfWeek: String,       // Serialized e.g. "Mon,Tue,Wed,Thu,Fri,Sat,Sun"
    val isEnabled: Boolean = true,
    val ringtone: String = "Serene Dawn",        // Preset name
    val vibrationPattern: String = "Classic Pulsing", // e.g. "None", "Classic Pulsing", "Rapid Heartbeat", "Smooth Wave"
    val mediaPreset: String = "Mountain Sunrise",  // Preset image/video slides: "Mountain Sunrise", "Makkah Mosque", "Peaceful Cosmos", "Ocean Ripples"
    val snoozeMinutes: Int = 5,
    val forceDismissMode: Boolean = false, // If true, requires solving a simple puzzle/equation to dismiss
    val isSnoozed: Boolean = false,
    val lastTriggeredTime: Long = 0L
)
