package com.example.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import com.example.data.preferences.BuiltInAlarmTones
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

object AlarmSoundManager {
    private var mediaPlayer: MediaPlayer? = null
    private var volumeRampJob: Job? = null

    fun playTone(
        context: Context,
        toneKey: String,
        volumePercent: Int,
        customAudioPath: String? = null,
        isLooping: Boolean = false,
        isAscendingVolume: Boolean = false
    ) {
        stopTone()

        try {
            val targetVolume = (volumePercent.coerceIn(0, 100) / 100f)
            val initialVolume = if (isAscendingVolume) (targetVolume * 0.2f).coerceAtLeast(0.08f) else targetVolume

            val player = MediaPlayer()
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )

            var isDataSourceSet = false

            // اگر زنگ انتخابی فایل صوتی شخصی گوشی باشد
            if (toneKey == "alarm_custom_file" && !customAudioPath.isNullOrBlank()) {
                val file = File(customAudioPath)
                if (file.exists() && file.length() > 0) {
                    player.setDataSource(file.absolutePath)
                    isDataSourceSet = true
                }
            }

            // فال‌بک یا زنگ‌های پیش‌ساخته سیستم
            if (!isDataSourceSet) {
                val toneOption = BuiltInAlarmTones.firstOrNull { it.key == toneKey } ?: BuiltInAlarmTones.first()
                val toneType = toneOption.ringtoneType ?: RingtoneManager.TYPE_ALARM
                val uri: Uri = RingtoneManager.getDefaultUri(toneType)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    ?: return
                player.setDataSource(context, uri)
            }

            player.isLooping = isLooping
            player.setVolume(initialVolume, initialVolume)
            player.prepare()
            player.start()
            mediaPlayer = player

            // در صورت فعال بودن صدای افزایشی: افزایش تدریجی صدا هر ۲ ثانیه
            if (isAscendingVolume && targetVolume > initialVolume) {
                volumeRampJob = CoroutineScope(Dispatchers.Main).launch {
                    var currentVol = initialVolume
                    val step = (targetVolume - initialVolume) / 5f
                    while (isActive && currentVol < targetVolume) {
                        delay(2000)
                        if (!isActive || mediaPlayer == null) break
                        currentVol = (currentVol + step).coerceAtMost(targetVolume)
                        try {
                            mediaPlayer?.setVolume(currentVol, currentVol)
                        } catch (_: Exception) {
                            break
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("AlarmSoundManager", "Error playing tone: ${e.message}", e)
        }
    }

    fun stopTone() {
        try {
            volumeRampJob?.cancel()
            volumeRampJob = null
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
        } catch (_: Exception) {
            mediaPlayer = null
        }
    }

    fun isPlaying(): Boolean {
        return try {
            mediaPlayer?.isPlaying == true
        } catch (_: Exception) {
            false
        }
    }
}
