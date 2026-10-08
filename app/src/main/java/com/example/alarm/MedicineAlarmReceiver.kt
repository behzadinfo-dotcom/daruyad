package com.example.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.MedRemindApplication
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

class MedicineAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val hour = intent.getIntExtra(EXTRA_BATCH_HOUR, 8)
        val minute = intent.getIntExtra(EXTRA_BATCH_MINUTE, 0)
        val medNames = intent.getStringExtra(EXTRA_MED_NAMES) ?: "داروهای زمان‌بندی شده"
        val repeatAttempt = intent.getIntExtra(EXTRA_REPEAT_ATTEMPT, 0)

        val timeString = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)

        // اینتنت مستقیم و مطمئن برای باز کردن لیست داروهای همان ساعت
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            action = "com.aistudio.medremind.OPEN_SESSION_${hour}_${minute}"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(MainActivity.EXTRA_TARGET_SCREEN, MainActivity.SCREEN_SESSION_DETAIL)
            putExtra(MainActivity.EXTRA_SESSION_HOUR, hour)
            putExtra(MainActivity.EXTRA_SESSION_MINUTE, minute)
        }

        val notificationId = hour * 100 + minute
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val repeatNotice = if (repeatAttempt > 0) " (یادآوری مجدد $repeatAttempt)" else ""

        val notification = NotificationCompat.Builder(context, MedRemindApplication.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⏰ نوبت مصرف داروی ساعت $timeString$repeatNotice")
            .setContentText("داروهای این نوبت: $medNames")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "سلام، وقت مصرف داروها فرارسیده است!\n" +
                                "داروهای این نوبت: $medNames\n" +
                                if (repeatAttempt > 0) "⚠️ به علت عدم ثبت مصرف، این یادآور تکرار شده است." else "" +
                                "برای مشاهده تصاویر، دوزها و تأیید سریع کلیک کنید."
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.mipmap.ic_launcher,
                "مشاهده لیست و ثبت مصرف",
                pendingIntent
            )
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)

        // پخش صدای زنگ با ولوم و صدای افزایشی تنظیم شده
        val app = context.applicationContext as? MedRemindApplication
        app?.let {
            CoroutineScope(Dispatchers.IO).launch {
                val settings = it.preferencesRepository.userSettingsFlow.first()
                if (settings.isSoundEnabled) {
                    AlarmSoundManager.playTone(
                        context = context,
                        toneKey = settings.alarmToneKey,
                        volumePercent = settings.alarmVolumePercent,
                        customAudioPath = settings.customAudioPath,
                        isLooping = false,
                        isAscendingVolume = settings.isAscendingVolume
                    )
                }

                // برنامه‌ریزی تکرار آلارم در صورت عدم مصرف (Repeat / Snooze)
                if (repeatAttempt < settings.alarmRepeatCount) {
                    val repeatIntervalMillis = settings.alarmRepeatIntervalMinutes.coerceAtLeast(1) * 60 * 1000L
                    it.alarmScheduler.scheduleExactAlarm(
                        hour = hour,
                        minute = minute,
                        triggerAtMillis = System.currentTimeMillis() + repeatIntervalMillis,
                        medicinesSummary = medNames,
                        repeatAttempt = repeatAttempt + 1
                    )
                }
            }
        }
    }

    companion object {
        const val ACTION_MEDICINE_ALARM = "com.aistudio.medremind.ACTION_MEDICINE_ALARM"
        const val EXTRA_BATCH_HOUR = "extra_batch_hour"
        const val EXTRA_BATCH_MINUTE = "extra_batch_minute"
        const val EXTRA_MED_NAMES = "extra_med_names"
        const val EXTRA_REPEAT_ATTEMPT = "extra_repeat_attempt"
    }
}
