package com.example.data.preferences

import android.content.Context
import android.media.RingtoneManager
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_settings")

enum class AppFontWeight(val label: String, val weightLevel: Int) {
    NORMAL("معمولی", 400),
    MEDIUM("متوسط", 500),
    BOLD("برجسته و بولد", 700)
}

enum class AppFontSize(val label: String, val scale: Float) {
    SMALL("ریز", 0.88f),
    MEDIUM("استاندارد", 1.0f),
    LARGE("درشت (خوانا)", 1.18f),
    EXTRA_LARGE("بسیار درشت (ویژه سالمندان)", 1.35f)
}

enum class PastelPalette(val label: String, val key: String) {
    MINT("نعنایی ملایم (سلامت)", "mint"),
    LAVENDER("یاسی و اسطوخودوس (آرامش)", "lavender"),
    PEACH("هلویی و گرم (سرزندگی)", "peach"),
    ROSE("رز ملایم (مهر)", "rose"),
    SKY("آبی آسمانی (روشن)", "sky")
}

data class AlarmToneOption(
    val key: String,
    val title: String,
    val description: String,
    val ringtoneType: Int? = null
)

val BuiltInAlarmTones = listOf(
    AlarmToneOption("alarm_standard", "زنگ ساعت و هشدار آلارم", "صدای رسا و استاندارد ساعت زنگ‌دار", RingtoneManager.TYPE_ALARM),
    AlarmToneOption("alarm_gentle", "نغمه ملایم و آرامش‌بخش", "ملودی ملایم اعلان مناسب افراد حساس", RingtoneManager.TYPE_NOTIFICATION),
    AlarmToneOption("alarm_ringtone", "زنگ تماس گوشی", "صدای ممتد و قابل توجه مانند زنگ تماس", RingtoneManager.TYPE_RINGTONE)
)

data class UserAppSettings(
    val fontScale: Float = 1.0f,
    val fontWeightLevel: Int = 400,
    val paletteKey: String = "mint",
    val roundingWindowMinutes: Int = 15,
    val isSoundEnabled: Boolean = true,
    val isVibrationEnabled: Boolean = true,
    val alarmToneKey: String = "alarm_standard",
    val alarmVolumePercent: Int = 85,
    val customAudioPath: String = "",
    val customAudioTitle: String = "",
    val isVisualAccessibilityMode: Boolean = false, // حالت مصور ویژه افراد کم‌سواد
    val alarmRepeatCount: Int = 3, // تعداد دفعات تکرار آلارم در صورت عدم مصرف (۰: خاموش، ۱، ۳، ۵، ۱۰)
    val alarmRepeatIntervalMinutes: Int = 5, // فاصله بین تکرارها (دقیقه)
    val isAscendingVolume: Boolean = true // صدای افزایشی (شروع آرام و افزایش تدریجی)
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val FONT_SCALE = floatPreferencesKey("font_scale")
        val FONT_WEIGHT = intPreferencesKey("font_weight")
        val PALETTE_KEY = stringPreferencesKey("palette_key")
        val ROUNDING_WINDOW = intPreferencesKey("rounding_window")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val ALARM_TONE_KEY = stringPreferencesKey("alarm_tone_key")
        val ALARM_VOLUME_PERCENT = intPreferencesKey("alarm_volume_percent")
        val CUSTOM_AUDIO_PATH = stringPreferencesKey("custom_audio_path")
        val CUSTOM_AUDIO_TITLE = stringPreferencesKey("custom_audio_title")
        val VISUAL_ACCESSIBILITY_MODE = booleanPreferencesKey("visual_accessibility_mode")
        val ALARM_REPEAT_COUNT = intPreferencesKey("alarm_repeat_count")
        val ALARM_REPEAT_INTERVAL = intPreferencesKey("alarm_repeat_interval")
        val IS_ASCENDING_VOLUME = booleanPreferencesKey("is_ascending_volume")
    }

    val userSettingsFlow: Flow<UserAppSettings> = context.dataStore.data.map { prefs ->
        UserAppSettings(
            fontScale = prefs[PreferencesKeys.FONT_SCALE] ?: 1.0f,
            fontWeightLevel = prefs[PreferencesKeys.FONT_WEIGHT] ?: 400,
            paletteKey = prefs[PreferencesKeys.PALETTE_KEY] ?: "mint",
            roundingWindowMinutes = prefs[PreferencesKeys.ROUNDING_WINDOW] ?: 15,
            isSoundEnabled = prefs[PreferencesKeys.SOUND_ENABLED] ?: true,
            isVibrationEnabled = prefs[PreferencesKeys.VIBRATION_ENABLED] ?: true,
            alarmToneKey = prefs[PreferencesKeys.ALARM_TONE_KEY] ?: "alarm_standard",
            alarmVolumePercent = prefs[PreferencesKeys.ALARM_VOLUME_PERCENT] ?: 85,
            customAudioPath = prefs[PreferencesKeys.CUSTOM_AUDIO_PATH] ?: "",
            customAudioTitle = prefs[PreferencesKeys.CUSTOM_AUDIO_TITLE] ?: "",
            isVisualAccessibilityMode = prefs[PreferencesKeys.VISUAL_ACCESSIBILITY_MODE] ?: false,
            alarmRepeatCount = prefs[PreferencesKeys.ALARM_REPEAT_COUNT] ?: 3,
            alarmRepeatIntervalMinutes = prefs[PreferencesKeys.ALARM_REPEAT_INTERVAL] ?: 5,
            isAscendingVolume = prefs[PreferencesKeys.IS_ASCENDING_VOLUME] ?: true
        )
    }

    suspend fun updateFontScale(scale: Float) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.FONT_SCALE] = scale
        }
    }

    suspend fun updateFontWeight(weightLevel: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.FONT_WEIGHT] = weightLevel
        }
    }

    suspend fun updatePalette(key: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.PALETTE_KEY] = key
        }
    }

    suspend fun updateRoundingWindow(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.ROUNDING_WINDOW] = minutes
        }
    }

    suspend fun updateSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun updateVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.VIBRATION_ENABLED] = enabled
        }
    }

    suspend fun updateAlarmTone(toneKey: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.ALARM_TONE_KEY] = toneKey
        }
    }

    suspend fun updateAlarmVolume(volumePercent: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.ALARM_VOLUME_PERCENT] = volumePercent.coerceIn(0, 100)
        }
    }

    suspend fun setCustomAudioTone(path: String, title: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.CUSTOM_AUDIO_PATH] = path
            prefs[PreferencesKeys.CUSTOM_AUDIO_TITLE] = title
            prefs[PreferencesKeys.ALARM_TONE_KEY] = "alarm_custom_file"
        }
    }

    suspend fun updateVisualAccessibilityMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.VISUAL_ACCESSIBILITY_MODE] = enabled
        }
    }

    suspend fun updateAlarmRepeatCount(count: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.ALARM_REPEAT_COUNT] = count
        }
    }

    suspend fun updateAlarmRepeatInterval(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.ALARM_REPEAT_INTERVAL] = minutes
        }
    }

    suspend fun updateAscendingVolume(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.IS_ASCENDING_VOLUME] = enabled
        }
    }
}
