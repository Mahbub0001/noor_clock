package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val note: String = "",
    val category: String, // e.g. "Study", "Work", "Personal", "Health", "Islamic worship"
    val isCompleted: Boolean = false,
    val dateCreated: Long = System.currentTimeMillis(),
    val streak: Int = 0,
    val lastCompletedDate: String = "", // formatted date String e.g. "2026-05-30" to track daily completion
    val needsReminder: Boolean = false,
    val isPersistentNotification: Boolean = false
)
