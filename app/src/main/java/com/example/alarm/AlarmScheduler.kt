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

class AlarmScheduler(
    private val context: Context,
    private val repository: MedicineRepository
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    suspend fun rescheduleAllSmartAlarms() = withContext(Dispatchers.IO) {
        try {
            // ایجاد نشست‌های تجمیعی امروز
            val today = Calendar.getInstance()
            val sessions = repository.generateDailySessions(today)
            val nowMillis = System.currentTimeMillis()

            for (session in sessions) {
                val targetCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, session.batchHour)
                    set(Calendar.MINUTE, session.batchMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // اگر زمان امروز گذشته، برای فردا ست شود
                if (targetCal.timeInMillis < nowMillis) {
                    targetCal.add(Calendar.DAY_OF_YEAR, 1)
                }

                val medNames = session.doseItems.joinToString("، ") { it.medicine.name }
                scheduleExactAlarm(
                    hour = session.batchHour,
                    minute = session.batchMinute,
                    triggerAtMillis = targetCal.timeInMillis,
                    medicinesSummary = medNames
                )
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
        repeatAttempt: Int = 0
    ) {
        val intent = Intent(context, MedicineAlarmReceiver::class.java).apply {
            action = MedicineAlarmReceiver.ACTION_MEDICINE_ALARM
            putExtra(MedicineAlarmReceiver.EXTRA_BATCH_HOUR, hour)
            putExtra(MedicineAlarmReceiver.EXTRA_BATCH_MINUTE, minute)
            putExtra(MedicineAlarmReceiver.EXTRA_MED_NAMES, medicinesSummary)
            putExtra(MedicineAlarmReceiver.EXTRA_REPEAT_ATTEMPT, repeatAttempt)
        }

        val requestCode = hour * 100 + minute + (repeatAttempt * 10000)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // در صورت عدم اعطای مجوز دقیق، فال‌بک به set
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    fun cancelAlarm(hour: Int, minute: Int) {
        val intent = Intent(context, MedicineAlarmReceiver::class.java).apply {
            action = MedicineAlarmReceiver.ACTION_MEDICINE_ALARM
        }
        val requestCode = hour * 100 + minute
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }
}
