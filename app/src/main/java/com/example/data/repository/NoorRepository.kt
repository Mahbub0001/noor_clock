package com.example.data.repository

import com.example.data.local.AlarmDao
import com.example.data.local.TaskDao
import com.example.data.model.Alarm
import com.example.data.model.Task
import kotlinx.coroutines.flow.Flow

class NoorRepository(
    private val alarmDao: AlarmDao,
    private val taskDao: TaskDao
) {
    // Alarms
    val allAlarms: Flow<List<Alarm>> = alarmDao.getAllAlarms()

    suspend fun getAlarmById(id: Int): Alarm? = alarmDao.getAlarmById(id)

    suspend fun insertAlarm(alarm: Alarm): Long = alarmDao.insertAlarm(alarm)

    suspend fun updateAlarm(alarm: Alarm) = alarmDao.updateAlarm(alarm)

    suspend fun deleteAlarm(alarm: Alarm) = alarmDao.deleteAlarm(alarm)

    // Tasks
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    fun getTasksByCategory(category: String): Flow<List<Task>> = taskDao.getTasksByCategory(category)

    suspend fun getTaskById(id: Int): Task? = taskDao.getTaskById(id)

    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)
}
