package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.alarm.AlarmScheduler
import com.example.data.local.IntakeLogEntity
import com.example.data.local.MedicineEntity
import com.example.data.preferences.UserAppSettings
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.ClusteredIntakeSession
import com.example.data.repository.MedicineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(
    private val repository: MedicineRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _dailySessions = MutableStateFlow<List<ClusteredIntakeSession>>(emptyList())
    val dailySessions: StateFlow<List<ClusteredIntakeSession>> = _dailySessions.asStateFlow()

    private val _isLoadingSessions = MutableStateFlow(false)
    val isLoadingSessions: StateFlow<Boolean> = _isLoadingSessions.asStateFlow()

    val allMedicines: StateFlow<List<MedicineEntity>> = repository.getAllMedicines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentLogs: StateFlow<List<IntakeLogEntity>> = repository.getRecentLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userSettings: StateFlow<UserAppSettings> = preferencesRepository.userSettingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserAppSettings())

    init {
        loadTodaySessions()
    }

    fun loadTodaySessions() {
        viewModelScope.launch {
            _isLoadingSessions.value = true
            try {
                val sessions = repository.generateDailySessions(
                    Calendar.getInstance(),
                    userSettings.value.roundingWindowMinutes
                )
                _dailySessions.value = sessions
                // نوبت‌هایی که همه دوزشان ثبت شده، دیگر نیاز به یادآور تکراری ندارند
                sessions.flatMap { it.doseItems }
                    .groupBy { AlarmScheduler.minuteOfDay(it.scheduledTime) }
                    .forEach { (minute, doses) ->
                        if (doses.none { it.status == "PENDING" }) alarmScheduler.cancelRepeats(minute)
                    }
            } catch (e: Exception) {
                _dailySessions.value = emptyList()
            } finally {
                _isLoadingSessions.value = false
            }
        }
    }

    fun markDoseStatus(logId: Long, status: String) {
        viewModelScope.launch {
            repository.updateLogStatus(logId, status)
            loadTodaySessions()
        }
    }

    fun recordPrnIntake(medicine: MedicineEntity) {
        viewModelScope.launch {
            repository.recordManualIntake(medicine.id, "TAKEN")
            loadTodaySessions()
        }
    }

    fun markAllInSession(session: ClusteredIntakeSession, status: String) {
        viewModelScope.launch {
            repository.markAllInSession(session.dateString, session.batchHour, session.batchMinute, status)
            loadTodaySessions()
        }
    }

    fun deleteMedicine(medicine: MedicineEntity) {
        viewModelScope.launch {
            repository.deleteMedicine(medicine)
            alarmScheduler.rescheduleAllSmartAlarms()
            loadTodaySessions()
        }
    }

    fun toggleMedicineActive(medicine: MedicineEntity) {
        viewModelScope.launch {
            repository.toggleMedicineActive(medicine.id, !medicine.isActive)
            alarmScheduler.rescheduleAllSmartAlarms()
            loadTodaySessions()
        }
    }

    fun updateFontScale(scale: Float) {
        viewModelScope.launch {
            preferencesRepository.updateFontScale(scale)
        }
    }

    fun updateFontWeight(weightLevel: Int) {
        viewModelScope.launch {
            preferencesRepository.updateFontWeight(weightLevel)
        }
    }

    fun updatePalette(paletteKey: String) {
        viewModelScope.launch {
            preferencesRepository.updatePalette(paletteKey)
        }
    }

    fun updateVisualAccessibilityMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateVisualAccessibilityMode(enabled)
        }
    }

    fun updateRoundingWindow(minutes: Int) {
        viewModelScope.launch {
            preferencesRepository.updateRoundingWindow(minutes)
            loadTodaySessions()
        }
    }

    fun updateAlarmTone(toneKey: String) {
        viewModelScope.launch {
            preferencesRepository.updateAlarmTone(toneKey)
        }
    }

    fun updateAlarmVolume(volumePercent: Int) {
        viewModelScope.launch {
            preferencesRepository.updateAlarmVolume(volumePercent)
        }
    }

    fun updateAlarmRepeatCount(count: Int) {
        viewModelScope.launch {
            preferencesRepository.updateAlarmRepeatCount(count)
        }
    }

    fun updateAlarmRepeatInterval(minutes: Int) {
        viewModelScope.launch {
            preferencesRepository.updateAlarmRepeatInterval(minutes)
        }
    }

    fun updateAscendingVolume(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateAscendingVolume(enabled)
        }
    }

    fun selectCustomAudioFile(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch {
            val result = com.example.util.AudioStorageUtil.saveCustomAudioUri(context, uri)
            if (result != null) {
                val (path, title) = result
                preferencesRepository.setCustomAudioTone(path, title)
            }
        }
    }

    private val _isPreviewPlaying = MutableStateFlow(false)
    val isPreviewPlaying: StateFlow<Boolean> = _isPreviewPlaying.asStateFlow()

    fun previewAlarmTone(context: android.content.Context, toneKey: String, volumePercent: Int, customPath: String? = null) {
        if (_isPreviewPlaying.value) {
            com.example.alarm.AlarmSoundManager.stopTone()
            _isPreviewPlaying.value = false
        } else {
            com.example.alarm.AlarmSoundManager.playTone(
                context = context,
                toneKey = toneKey,
                volumePercent = volumePercent,
                customAudioPath = customPath,
                isLooping = false
            )
            _isPreviewPlaying.value = true
        }
    }

    fun stopAlarmTonePreview() {
        com.example.alarm.AlarmSoundManager.stopTone()
        _isPreviewPlaying.value = false
    }

    override fun onCleared() {
        super.onCleared()
        com.example.alarm.AlarmSoundManager.stopTone()
    }

    fun triggerTestAlarm() {
        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val minute = now.get(Calendar.MINUTE)
        val testNames = if (_dailySessions.value.isNotEmpty()) {
            _dailySessions.value.first().doseItems.joinToString("، ") { it.medicine.name }
        } else {
            "آموکسی‌سیلین، امپرازول (تست آلارم)"
        }
        alarmScheduler.scheduleExactAlarm(
            hour = hour,
            minute = minute,
            triggerAtMillis = System.currentTimeMillis() + 3000, // ۳ ثانیه بعد
            medicinesSummary = testNames,
            isTest = true
        )
    }

    class Factory(
        private val repository: MedicineRepository,
        private val preferencesRepository: UserPreferencesRepository,
        private val alarmScheduler: AlarmScheduler
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(repository, preferencesRepository, alarmScheduler) as T
        }
    }
}
