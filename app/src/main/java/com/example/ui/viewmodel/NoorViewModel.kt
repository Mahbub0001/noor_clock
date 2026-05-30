package com.example.ui.viewmodel

import android.app.Application
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Alarm
import com.example.data.model.Task
import com.example.data.repository.NoorRepository
import com.example.ui.theme.AppThemeName
import com.example.util.GeminiQuotesClient
import com.example.util.PrayerTimeCalculator
import com.example.util.WeatherClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class NoorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NoorRepository

    // Base flows from DB
    val alarms: StateFlow<List<Alarm>>
    val tasks: StateFlow<List<Task>>

    // Selected location coordinates (Default: Makkah Al-Mukarramah coordinates)
    private val _currentCity = MutableStateFlow("Makkah")
    val currentCity: StateFlow<String> = _currentCity.asStateFlow()

    private val _latitude = MutableStateFlow(21.4267)
    val latitude: StateFlow<Double> = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow(39.8261)
    val longitude: StateFlow<Double> = _longitude.asStateFlow()

    private val _prayerMethod = MutableStateFlow(PrayerTimeCalculator.CalculationMethod.MAKKAH)
    val prayerMethod: StateFlow<PrayerTimeCalculator.CalculationMethod> = _prayerMethod.asStateFlow()

    // Calculated Prayer Times
    private val _prayerTimes = MutableStateFlow<PrayerTimeCalculator.PrayerTimes?>(null)
    val prayerTimes: StateFlow<PrayerTimeCalculator.PrayerTimes?> = _prayerTimes.asStateFlow()

    // Current Live Weather Status
    private val _temperature = MutableStateFlow(35.0)
    val temperature: StateFlow<Double> = _temperature.asStateFlow()

    private val _weatherCondition = MutableStateFlow("Sunny / Clear")
    val weatherCondition: StateFlow<String> = _weatherCondition.asStateFlow()

    private val _isWeatherLoading = MutableStateFlow(false)
    val isWeatherLoading: StateFlow<Boolean> = _isWeatherLoading.asStateFlow()

    // Global Geocoding Search States
    private val _searchResults = MutableStateFlow<List<com.example.util.LocationGeocodingClient.GeocodingResult>>(emptyList())
    val searchResults: StateFlow<List<com.example.util.LocationGeocodingClient.GeocodingResult>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // Active Triggering Alarm State
    private val _triggeredAlarm = MutableStateFlow<Alarm?>(null)
    val triggeredAlarm: StateFlow<Alarm?> = _triggeredAlarm.asStateFlow()

    // Math equation for force dismiss
    private val _dismissMathQuestion = MutableStateFlow("")
    val dismissMathQuestion: StateFlow<String> = _dismissMathQuestion.asStateFlow()
    private var correctMathResult = 0

    // Theme selector
    private val _activeTheme = MutableStateFlow(AppThemeName.FROSTED_GLASS)
    val activeTheme: StateFlow<AppThemeName> = _activeTheme.asStateFlow()

    // Settings
    private val _isQuietHours = MutableStateFlow(false)
    val isQuietHours: StateFlow<Boolean> = _isQuietHours.asStateFlow()

    private val _adhanAlerthandler = MutableStateFlow(true) // Play Adhan popup when prayer time hits
    val adhanAlertHandler: StateFlow<Boolean> = _adhanAlerthandler.asStateFlow()

    private val _snoozeMinutesRule = MutableStateFlow(5)
    val snoozeMinutesRule: StateFlow<Int> = _snoozeMinutesRule.asStateFlow()

    private val _persistentReminders = MutableStateFlow(true)
    val persistentReminders: StateFlow<Boolean> = _persistentReminders.asStateFlow()

    // AI Inspirational text
    private val _aiInspirationalQuote = MutableStateFlow("Begin your day beautifully with high focus, gratitude, and consistency in small acts.")
    val aiInspirationalQuote: StateFlow<String> = _aiInspirationalQuote.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Sound alert generator
    private var toneGenerator: ToneGenerator? = null
    private var soundPlayingJob: kotlinx.coroutines.Job? = null
    private var mediaPlayer: MediaPlayer? = null

    // Custom Ringtones state engine
    data class CustomRingtone(val path: String, val name: String)
    private val _customRingtones = MutableStateFlow<List<CustomRingtone>>(emptyList())
    val customRingtones: StateFlow<List<CustomRingtone>> = _customRingtones.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = NoorRepository(database.alarmDao(), database.taskDao())

        // Collect flows to represent view state
        alarms = repository.allAlarms.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        tasks = repository.allTasks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Setup tone generator for alarms
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 80)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Load custom ringtones
        try {
            loadCustomRingtones()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Run default calculations
        recalculatePrayerTimesAndWeather()

        // Start Background Alarm Continuous Ticker Scanning
        startAlarmScanningLoop()

        // Prepopulate tasks if empty
        viewModelScope.launch(Dispatchers.IO) {
            val databaseFile = AppDatabase.getDatabase(application)
            // check if task count is 0, add default ones
            val firstTaskState = databaseFile.taskDao().getAllTasks().firstOrNull() ?: emptyList()
            if (firstTaskState.isEmpty()) {
                val defaultTasks = listOf(
                    Task(title = "Morning Suhoor & Reflection", category = "Islamic worship", note = "Review goals and have a healthy breakfast before Fajr"),
                    Task(title = "Daily Focus Session: Mobile Project", category = "Study", note = "Work on modern Material 3 fluid widgets"),
                    Task(title = "Maintain 3k Step Refresh Walk", category = "Health", note = "Enjoy nature breeze afternoon"),
                    Task(title = "Complete Team Progress Sync", category = "Work", note = "Log tasks and check off checklist deliverables"),
                    Task(title = "Memorize 3 Verses of Surah Kahf", category = "Islamic worship", note = "Read and understand Tafsir for serene reflection")
                )
                defaultTasks.forEach { databaseFile.taskDao().insertTask(it) }
            }

            // prepopulate one default alarm
            val firstAlarmState = databaseFile.alarmDao().getAllAlarms().firstOrNull() ?: emptyList()
            if (firstAlarmState.isEmpty()) {
                databaseFile.alarmDao().insertAlarm(
                    Alarm(
                        hour = 5,
                        minute = 15,
                        label = "Fajr Awakening Sunrise",
                        daysOfWeek = "Mon,Tue,Wed,Thu,Fri,Sat,Sun",
                        ringtone = "Serene Dawn",
                        vibrationPattern = "Smooth Wave",
                        mediaPreset = "Mountain Sunrise",
                        snoozeMinutes = 5,
                        forceDismissMode = true
                    )
                )
            }
        }
    }

    // Recalculates prayer times astronomical offline math + weather REST trigger
    fun updateLocation(city: String, lat: Double, lon: Double) {
        _currentCity.value = city
        _latitude.value = lat
        _longitude.value = lon
        recalculatePrayerTimesAndWeather()
    }

    // Triggers global network search of city geometries anywhere in the world
    fun searchLocationsInWorld(query: String) {
        viewModelScope.launch {
            if (query.trim().length < 2) {
                _searchResults.value = emptyList()
                return@launch
            }
            _isSearching.value = true
            val results = com.example.util.LocationGeocodingClient.searchLocation(query)
            _searchResults.value = results
            _isSearching.value = false
        }
    }

    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }

    fun updatePrayerMethod(method: PrayerTimeCalculator.CalculationMethod) {
        _prayerMethod.value = method
        recalculatePrayerTimesAndWeather()
    }

    fun updateTheme(theme: AppThemeName) {
        _activeTheme.value = theme
    }

    fun triggerAiVibeCheck() {
        viewModelScope.launch {
            _isAiLoading.value = true
            val quote = GeminiQuotesClient.generateInspirationalQuote(
                _temperature.value,
                _weatherCondition.value,
                _activeTheme.value.displayName
            )
            _aiInspirationalQuote.value = quote
            _isAiLoading.value = false
        }
    }

    fun recalculatePrayerTimesAndWeather() {
        // Calculate offline-first prayer times
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val zoneId = TimeZone.getDefault().rawOffset / (1000.0 * 60.0 * 60.0)

        val times = PrayerTimeCalculator.calculateTimes(
            year = year,
            month = month,
            day = day,
            latitude = _latitude.value,
            longitude = _longitude.value,
            timeZoneOffsetHrs = zoneId,
            method = _prayerMethod.value
        )
        _prayerTimes.value = times

        // Fetch Live weather from Rest API
        viewModelScope.launch(Dispatchers.IO) {
            _isWeatherLoading.value = true
            val weatherData = WeatherClient.fetchWeather(_latitude.value, _longitude.value)
            _temperature.value = weatherData.first
            _weatherCondition.value = weatherData.second
            _isWeatherLoading.value = false
        }
    }

    // --- ALARM TICKER ENGINE ---
    private fun startAlarmScanningLoop() {
        viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                // Read current hour, minute and day of week
                val cal = Calendar.getInstance()
                val currentHour = cal.get(Calendar.HOUR_OF_DAY)
                val currentMinute = cal.get(Calendar.MINUTE)
                val currentSecond = cal.get(Calendar.SECOND)
                val dayOfWeekNum = cal.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...
                val dayStr = when(dayOfWeekNum) {
                    Calendar.SUNDAY -> "Sun"
                    Calendar.MONDAY -> "Mon"
                    Calendar.TUESDAY -> "Tue"
                    Calendar.WEDNESDAY -> "Wed"
                    Calendar.THURSDAY -> "Thu"
                    Calendar.FRIDAY -> "Fri"
                    Calendar.SATURDAY -> "Sat"
                    else -> ""
                }

                if (currentSecond == 0 && _triggeredAlarm.value == null && !_isQuietHours.value) {
                    // Search active alarms in memory
                    val activeAlarms = alarms.value.filter { it.isEnabled }
                    for (alarm in activeAlarms) {
                        // Check if time matches
                        if (alarm.hour == currentHour && alarm.minute == currentMinute) {
                            // Check if current day is selected in daysOfWeek
                            val listDays = alarm.daysOfWeek.split(",")
                            if (listDays.contains(dayStr) || alarm.daysOfWeek.trim().isEmpty() || alarm.daysOfWeek == "Everyday") {
                                // Assert cooldown: trigger only if last triggered is more than 60 seconds ago
                                val now = System.currentTimeMillis()
                                if (now - alarm.lastTriggeredTime > 60000L) {
                                    triggerAlarmNow(alarm)
                                    break
                                }
                            }
                        }
                    }
                }
                delay(1000) // Scan every 1 second
            }
        }
    }

    private fun triggerAlarmNow(alarm: Alarm) {
        viewModelScope.launch(Dispatchers.Main) {
            _triggeredAlarm.value = alarm
            if (alarm.forceDismissMode) {
                generateDismissMathQuestion()
            }
            startAlarmSoundGenerator()

            // Update database to save last triggered time
            val updated = alarm.copy(lastTriggeredTime = System.currentTimeMillis())
            viewModelScope.launch(Dispatchers.IO) {
                repository.updateAlarm(updated)
            }
        }
    }

    private fun generateDismissMathQuestion() {
        val r = Random()
        val num1 = r.nextInt(12) + 4
        val num2 = r.nextInt(9) + 3
        correctMathResult = num1 + num2
        _dismissMathQuestion.value = "$num1 + $num2 = ?"
    }

    fun verifyMathAnswer(answer: String): Boolean {
        val ans = answer.trim().toIntOrNull()
        return if (ans == correctMathResult) {
            dismissAlarmOnly()
            true
        } else {
            generateDismissMathQuestion() // Generate a new one if incorrect
            false
        }
    }

    private fun startAlarmSoundGenerator() {
        soundPlayingJob?.cancel()
        mediaPlayer?.let {
            try {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mediaPlayer = null

        val alarm = _triggeredAlarm.value
        val ringtoneOption = alarm?.ringtone ?: "Serene Dawn"
        val isCustomFile = ringtoneOption.startsWith("/") || ringtoneOption.startsWith("content://")

        soundPlayingJob = viewModelScope.launch(Dispatchers.Default) {
            val context = getApplication<Application>()
            if (isCustomFile) {
                try {
                    val uri = if (ringtoneOption.startsWith("content://")) {
                        Uri.parse(ringtoneOption)
                    } else {
                        Uri.fromFile(File(ringtoneOption))
                    }
                    val mp = MediaPlayer().apply {
                        setDataSource(context, uri)
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        isLooping = true
                        prepare()
                        start()
                    }
                    mediaPlayer = mp
                } catch (e: Exception) {
                    e.printStackTrace()
                    playFallbackSynthLoop()
                }
            } else {
                playCustomMelody(ringtoneOption)
            }
        }
    }

    private suspend fun playCustomMelody(preset: String) {
        val tg = toneGenerator ?: return
        while (true) {
            try {
                when (preset) {
                    "Birds Melody" -> {
                        tg.startTone(ToneGenerator.TONE_CDMA_PIP, 100)
                        delay(150)
                        tg.startTone(ToneGenerator.TONE_CDMA_PIP, 100)
                        delay(150)
                        tg.startTone(ToneGenerator.TONE_CDMA_PIP, 120)
                        delay(800)
                    }
                    "Gentle Harp" -> {
                        tg.startTone(ToneGenerator.TONE_CDMA_CONFIRM, 150)
                        delay(350)
                        tg.startTone(ToneGenerator.TONE_CDMA_PIP, 100)
                        delay(250)
                        tg.startTone(ToneGenerator.TONE_CDMA_CONFIRM, 150)
                        delay(1200)
                    }
                    "Melodious Echo" -> {
                        tg.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 250)
                        delay(350)
                        tg.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150)
                        delay(1200)
                    }
                    else -> { // "Serene Dawn"
                        tg.startTone(ToneGenerator.TONE_CDMA_PIP, 450)
                        delay(900)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                delay(1000)
            }
        }
    }

    private suspend fun playFallbackSynthLoop() {
        val tg = toneGenerator ?: return
        while (true) {
            try {
                tg.startTone(ToneGenerator.TONE_CDMA_PIP, 450)
                delay(900)
            } catch (e: Exception) {
                e.printStackTrace()
                delay(1000)
            }
        }
    }

    private fun stopAlarmSoundGenerator() {
        soundPlayingJob?.cancel()
        soundPlayingJob = null
        mediaPlayer?.let {
            try {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mediaPlayer = null
    }

    fun loadCustomRingtones() {
        val context = getApplication<Application>()
        val dir = File(context.filesDir, "custom_ringtones")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val files = dir.listFiles() ?: emptyArray()
        val list = files.map { file ->
            CustomRingtone(
                path = file.absolutePath,
                name = file.nameWithoutExtension.replace('_', ' ')
            )
        }
        _customRingtones.value = list
    }

    fun addCustomRingtone(uri: Uri): String? {
        val context = getApplication<Application>()
        val contentResolver = context.contentResolver
        var displayName = "Custom Tone_${System.currentTimeMillis()}"

        try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameCol = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameCol != -1) {
                        val nameValue = cursor.getString(nameCol)
                        if (!nameValue.isNullOrBlank()) {
                            displayName = nameValue
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val dir = File(context.filesDir, "custom_ringtones")
            if (!dir.exists()) {
                dir.mkdirs()
            }

            var destFile = File(dir, displayName)
            if (destFile.exists()) {
                val ext = displayName.substringAfterLast('.', "")
                val base = displayName.substringBeforeLast('.')
                destFile = File(dir, "${base}_${System.currentTimeMillis()}.${ext}")
            }

            contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            loadCustomRingtones()
            return destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun deleteCustomRingtone(path: String) {
        try {
            val file = File(path)
            if (file.exists() && file.parentFile?.name == "custom_ringtones") {
                file.delete()
            }
            loadCustomRingtones()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Dismiss alarm
    fun dismissAlarmOnly() {
        stopAlarmSoundGenerator()
        _triggeredAlarm.value = null
    }

    // Snooze Alarm
    fun snoozeAlarm() {
        stopAlarmSoundGenerator()
        val alarm = _triggeredAlarm.value
        _triggeredAlarm.value = null

        if (alarm != null) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.MINUTE, alarm.snoozeMinutes)
            val snoozedHour = cal.get(Calendar.HOUR_OF_DAY)
            val snoozedMinute = cal.get(Calendar.MINUTE)

            val snoozedAlarm = alarm.copy(
                hour = snoozedHour,
                minute = snoozedMinute,
                isEnabled = true,
                // temporarily lower the cooldown constraint
                lastTriggeredTime = 0L,
                label = "${alarm.label} (Snoozed)"
            )
            viewModelScope.launch(Dispatchers.IO) {
                repository.insertAlarm(snoozedAlarm)
            }
        }
    }

    // Settings adjustments
    fun setQuietHours(enabled: Boolean) {
        _isQuietHours.value = enabled
    }

    fun setAdhanAlerthandler(enabled: Boolean) {
        _adhanAlerthandler.value = enabled
    }

    fun setSnoozeMinutesRule(minutes: Int) {
        _snoozeMinutesRule.value = minutes
    }

    fun setPersistentReminders(enabled: Boolean) {
        _persistentReminders.value = enabled
    }

    // --- ALARM CRUD OPERATIONS ---
    fun saveAlarm(alarm: Alarm, onDone: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            if (alarm.id == 0) {
                repository.insertAlarm(alarm)
            } else {
                repository.updateAlarm(alarm)
            }
            viewModelScope.launch(Dispatchers.Main) { onDone() }
        }
    }

    fun toggleAlarmEnabled(alarm: Alarm) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateAlarm(alarm.copy(isEnabled = !alarm.isEnabled))
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAlarm(alarm)
        }
    }

    // --- TASK CRUD OPERATIONS ---
    fun insertTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertTask(task)
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            val isChecked = !task.isCompleted
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            var currentStreak = task.streak
            if (isChecked) {
                // If checking complete, assert if streak increases
                val lastDate = task.lastCompletedDate
                if (lastDate.isEmpty()) {
                    currentStreak = 1
                } else {
                    // Basic day distance calculation
                    val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    try {
                        val d1 = df.parse(lastDate)
                        val d2 = df.parse(todayStr)
                        val diff = (d2.time - d1.time) / (1000 * 60 * 60 * 24)
                        if (diff <= 1L) {
                            currentStreak += 1
                        } else {
                            currentStreak = 1
                        }
                    } catch (e: Exception) {
                        currentStreak = 1
                    }
                }
            }

            repository.updateTask(task.copy(
                isCompleted = isChecked,
                streak = currentStreak,
                lastCompletedDate = if (isChecked) todayStr else task.lastCompletedDate
            ))
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTask(task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTask(task)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAlarmSoundGenerator()
        toneGenerator?.release()
    }
}
