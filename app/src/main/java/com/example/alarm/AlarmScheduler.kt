package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.repository.MedicineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * زمان‌بندی آلارم‌ها.
 * هر دوز دقیقاً در زمان تجویزی خودش یادآوری می‌شود (بدون میانگین‌گیری).
 * کد درخواست هر آلارم = دقیقهٔ روز * 100 + شمارهٔ تکرار، تا تکرارها قابل لغو باشند.
 */
class AlarmScheduler(
    private val context: Context,
    private val repository: MedicineRepository
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val prefs = context.getSharedPreferences("alarm_slots", Context.MODE_PRIVATE)

    fun canScheduleExact(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    /** همه آلارم‌های قبلی را لغو و آلارم‌های فعلی را برای نوبت بعدی هر دوز ثبت می‌کند. */
    suspend fun rescheduleAllSmartAlarms() = withContext(Dispatchers.IO) {
        try {
            cancelAllKnownSlots()

            val sessions = repository.generateDailySessions(Calendar.getInstance())
            val nowMillis = System.currentTimeMillis()

            // نام داروهای هر دقیقه (هر دوز در زمان دقیق خودش)
            val namesByMinute = linkedMapOf<Int, MutableList<String>>()
            for (session in sessions) {
                for (dose in session.doseItems) {
                    namesByMinute.getOrPut(minuteOfDay(dose.scheduledTime)) { mutableListOf() }
                        .add(dose.medicine.name)
                }
            }

            for ((minute, names) in namesByMinute) {
                val hour = minute / 60
                val min = minute % 60
                scheduleExactAlarm(
                    hour = hour,
                    minute = min,
                    triggerAtMillis = nextOccurrence(hour, min, nowMillis),
                    medicinesSummary = names.distinct().joinToString("، ")
                )
                rememberSlot(minute)
            }
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "Error rescheduling smart alarms: ${e.message}", e)
        }
    }

    fun scheduleExactAlarm(
        hour: Int,
        minute: Int,
        triggerAtMillis: Long,
        medicinesSummary: String,
        repeatAttempt: Int = 0,
        isTest: Boolean = false
    ) {
        val intent = Intent(context, MedicineAlarmReceiver::class.java).apply {
            action = MedicineAlarmReceiver.ACTION_MEDICINE_ALARM
            putExtra(MedicineAlarmReceiver.EXTRA_BATCH_HOUR, hour)
            putExtra(MedicineAlarmReceiver.EXTRA_BATCH_MINUTE, minute)
            putExtra(MedicineAlarmReceiver.EXTRA_MED_NAMES, medicinesSummary)
            putExtra(MedicineAlarmReceiver.EXTRA_REPEAT_ATTEMPT, repeatAttempt)
            putExtra(MedicineAlarmReceiver.EXTRA_IS_TEST, isTest)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode(hour * 60 + minute, repeatAttempt),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } catch (e: SecurityException) {
            // مجوز آلارم دقیق داده نشده: فال‌بک غیردقیق (کاربر باید از تنظیمات مجوز بدهد)
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    /** لغو تکرارهای یک نوبت (شماره تکرار ۱ به بالا). نوبت اصلی روزانه دست‌نخورده می‌ماند. */
    fun cancelRepeats(minuteOfDay: Int) {
        for (r in 1..MAX_REPEAT) cancelRequest(minuteOfDay, r)
    }

    /** لغو کامل یک نوبت، شامل نوبت روزانه. */
    fun cancelAlarm(hour: Int, minute: Int) {
        val minuteOfDay = hour * 60 + minute
        for (r in 0..MAX_REPEAT) cancelRequest(minuteOfDay, r)
    }

    private fun cancelRequest(minuteOfDay: Int, repeatAttempt: Int) {
        val intent = Intent(context, MedicineAlarmReceiver::class.java).apply {
            action = MedicineAlarmReceiver.ACTION_MEDICINE_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode(minuteOfDay, repeatAttempt),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun cancelAllKnownSlots() {
        val known = prefs.getStringSet(KEY_SLOTS, emptySet()) ?: emptySet()
        for (s in known) {
            s.toIntOrNull()?.let { cancelAlarm(it / 60, it % 60) }
        }
        prefs.edit().remove(KEY_SLOTS).apply()
    }

    private fun rememberSlot(minuteOfDay: Int) {
        val current = prefs.getStringSet(KEY_SLOTS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(minuteOfDay.toString())
        prefs.edit().putStringSet(KEY_SLOTS, current).apply()
    }

    companion object {
        private const val KEY_SLOTS = "scheduled_minutes"
        const val MAX_REPEAT = 50

        fun requestCode(minuteOfDay: Int, repeatAttempt: Int): Int = minuteOfDay * 100 + repeatAttempt

        fun minuteOfDay(timestamp: Long): Int {
            val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
            return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        }

        /** نزدیک‌ترین زمان آینده برای ساعت:دقیقه مشخص. */
        fun nextOccurrence(hour: Int, minute: Int, nowMillis: Long): Long {
            val cal = Calendar.getInstance().apply {
                timeInMillis = nowMillis
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (cal.timeInMillis <= nowMillis) cal.add(Calendar.DAY_OF_YEAR, 1)
            return cal.timeInMillis
        }
    }
}
