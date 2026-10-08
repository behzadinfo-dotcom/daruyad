package com.example.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.MedRemindApplication
import com.example.R
import com.example.data.repository.MedicineDoseItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class MedicineAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val hour = intent.getIntExtra(EXTRA_BATCH_HOUR, 8)
        val minute = intent.getIntExtra(EXTRA_BATCH_MINUTE, 0)
        val medNames = intent.getStringExtra(EXTRA_MED_NAMES) ?: "داروهای زمان‌بندی شده"
        val repeatAttempt = intent.getIntExtra(EXTRA_REPEAT_ATTEMPT, 0)
        val isTest = intent.getBooleanExtra(EXTRA_IS_TEST, false)
        val action = intent.action

        // goAsync: تا پایان کارهای دیتابیس و پخش صدا، سیستم پروسه را نمی‌کشد
        val pendingResult = goAsync()
        val app = context.applicationContext as? MedRemindApplication

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (app == null) return@launch
                when {
                    action == ACTION_MARK_TAKEN -> markPendingTaken(context, app, hour, minute)
                    action == ACTION_SNOOZE -> snooze(context, app, hour, minute, medNames, repeatAttempt)
                    isTest -> notify(context, hour, minute, medNames, repeatAttempt)
                    else -> handleScheduledAlarm(context, app, hour, minute, repeatAttempt)
                }
            } catch (e: Exception) {
                Log.e("MedicineAlarm", "Alarm handling failed: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleScheduledAlarm(
        context: Context,
        app: MedRemindApplication,
        hour: Int,
        minute: Int,
        repeatAttempt: Int
    ) {
        val minuteOfDay = hour * 60 + minute
        val settings = app.preferencesRepository.userSettingsFlow.first()

        // دوزهایی که الان در این دقیقه باید مصرف شوند (از دیتابیس، نه از متن قدیمی آلارم)
        val doses: List<MedicineDoseItem> = app.medicineRepository
            .generateDailySessions(Calendar.getInstance(), settings.roundingWindowMinutes)
            .flatMap { it.doseItems }
            .filter { AlarmScheduler.minuteOfDay(it.scheduledTime) == minuteOfDay }

        // داروی این دقیقه دیگر فعال نیست: کاری نکن
        if (doses.isEmpty()) return

        // نوبت اصلی: همیشه برای فردا همین ساعت دوباره ثبت شود (یادآور روزانه)
        if (repeatAttempt == 0) {
            app.alarmScheduler.scheduleExactAlarm(
                hour = hour,
                minute = minute,
                triggerAtMillis = AlarmScheduler.nextOccurrence(hour, minute, System.currentTimeMillis() + 60_000),
                medicinesSummary = doses.joinToString("، ") { it.medicine.name }
            )
        }

        val pending = doses.filter { it.status == "PENDING" }
        if (pending.isEmpty()) {
            // همه دوزها ثبت شده‌اند: تکرار لازم نیست
            app.alarmScheduler.cancelRepeats(minuteOfDay)
            return
        }

        val names = pending.joinToString("، ") { it.medicine.name }
        notify(context, hour, minute, names, repeatAttempt)

        if (settings.isVibrationEnabled) vibrate(context)

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

        // تکرار فقط تا وقتی مصرف ثبت نشده و به سقف تعداد نرسیده‌ایم
        if (repeatAttempt < settings.alarmRepeatCount) {
            val intervalMillis = settings.alarmRepeatIntervalMinutes.coerceAtLeast(1) * 60 * 1000L
            app.alarmScheduler.scheduleExactAlarm(
                hour = hour,
                minute = minute,
                triggerAtMillis = System.currentTimeMillis() + intervalMillis,
                medicinesSummary = names,
                repeatAttempt = repeatAttempt + 1
            )
        }
    }

    /** دکمهٔ «مصرف شد» در اعلان: همهٔ دوزهای در انتظار همان دقیقه ثبت می‌شوند. */
    private suspend fun markPendingTaken(context: Context, app: MedRemindApplication, hour: Int, minute: Int) {
        val minuteOfDay = hour * 60 + minute
        app.medicineRepository.generateDailySessions(Calendar.getInstance())
            .flatMap { it.doseItems }
            .filter { AlarmScheduler.minuteOfDay(it.scheduledTime) == minuteOfDay && it.status == "PENDING" }
            .forEach { app.medicineRepository.updateLogStatus(it.intakeLogId, "TAKEN") }
        app.alarmScheduler.cancelRepeats(minuteOfDay)
        cancelNotification(context, hour, minute)
    }

    /** دکمهٔ «۱۰ دقیقه بعد» در اعلان. */
    private fun snooze(
        context: Context,
        app: MedRemindApplication,
        hour: Int,
        minute: Int,
        medNames: String,
        repeatAttempt: Int
    ) {
        app.alarmScheduler.scheduleExactAlarm(
            hour = hour,
            minute = minute,
            triggerAtMillis = System.currentTimeMillis() + SNOOZE_MINUTES * 60_000L,
            medicinesSummary = medNames,
            repeatAttempt = repeatAttempt + 1
        )
        cancelNotification(context, hour, minute)
    }

    private fun notify(
        context: Context,
        hour: Int,
        minute: Int,
        medNames: String,
        repeatAttempt: Int
    ) {
        val timeString = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
        val notificationId = hour * 100 + minute

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            action = "com.aistudio.medremind.OPEN_SESSION_${hour}_${minute}"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(MainActivity.EXTRA_TARGET_SCREEN, MainActivity.SCREEN_SESSION_DETAIL)
            putExtra(MainActivity.EXTRA_SESSION_HOUR, hour)
            putExtra(MainActivity.EXTRA_SESSION_MINUTE, minute)
        }
        val openPI = PendingIntent.getActivity(
            context, notificationId, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val takenPI = PendingIntent.getBroadcast(
            context, notificationId * 10 + 1,
            Intent(context, MedicineAlarmReceiver::class.java).apply {
                action = ACTION_MARK_TAKEN
                putExtra(EXTRA_BATCH_HOUR, hour)
                putExtra(EXTRA_BATCH_MINUTE, minute)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozePI = PendingIntent.getBroadcast(
            context, notificationId * 10 + 2,
            Intent(context, MedicineAlarmReceiver::class.java).apply {
                action = ACTION_SNOOZE
                putExtra(EXTRA_BATCH_HOUR, hour)
                putExtra(EXTRA_BATCH_MINUTE, minute)
                putExtra(EXTRA_MED_NAMES, medNames)
                putExtra(EXTRA_REPEAT_ATTEMPT, repeatAttempt)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val repeatNotice = if (repeatAttempt > 0) " (یادآوری مجدد $repeatAttempt)" else ""
        val bigText = listOfNotNull(
            "سلام، وقت مصرف داروها فرا رسیده است.",
            "داروهای این نوبت: $medNames",
            if (repeatAttempt > 0) "⚠️ مصرف هنوز ثبت نشده؛ این یادآور تکرار شده است." else null,
            "می‌توانید با دکمه‌های زیر مصرف را ثبت یا یادآوری را به تعویق بیندازید."
        ).joinToString("\n")

        val notification = NotificationCompat.Builder(context, MedRemindApplication.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⏰ نوبت مصرف داروی ساعت $timeString$repeatNotice")
            .setContentText("داروهای این نوبت: $medNames")
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(openPI)
            .addAction(R.mipmap.ic_launcher, "مصرف شد ✓", takenPI)
            .addAction(R.mipmap.ic_launcher, "۱۰ دقیقه بعد", snoozePI)
            .addAction(R.mipmap.ic_launcher, "مشاهده", openPI)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    private fun cancelNotification(context: Context, hour: Int, minute: Int) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(hour * 100 + minute)
    }

    @Suppress("DEPRECATION")
    private fun vibrate(context: Context) {
        try {
            val pattern = longArrayOf(0, 600, 400, 600, 400, 600)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    v.vibrate(pattern, -1)
                }
            }
        } catch (e: Exception) {
            Log.e("MedicineAlarm", "Vibration failed: ${e.message}")
        }
    }

    companion object {
        const val ACTION_MEDICINE_ALARM = "com.aistudio.medremind.ACTION_MEDICINE_ALARM"
        const val ACTION_MARK_TAKEN = "com.aistudio.medremind.ACTION_MARK_TAKEN"
        const val ACTION_SNOOZE = "com.aistudio.medremind.ACTION_SNOOZE"
        const val EXTRA_BATCH_HOUR = "extra_batch_hour"
        const val EXTRA_BATCH_MINUTE = "extra_batch_minute"
        const val EXTRA_MED_NAMES = "extra_med_names"
        const val EXTRA_REPEAT_ATTEMPT = "extra_repeat_attempt"
        const val EXTRA_IS_TEST = "extra_is_test"
        private const val SNOOZE_MINUTES = 10L
    }
}
