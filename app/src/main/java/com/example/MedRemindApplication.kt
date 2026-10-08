package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.alarm.AlarmScheduler
import com.example.data.local.AppDatabase
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.MedicineRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MedRemindApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy { AppDatabase.getDatabase(this) }
    val medicineRepository by lazy { MedicineRepository(database.medicineDao(), database.intakeLogDao()) }
    val preferencesRepository by lazy { UserPreferencesRepository(this) }
    val alarmScheduler by lazy { AlarmScheduler(this, medicineRepository) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()

        applicationScope.launch {
            alarmScheduler.rescheduleAllSmartAlarms()
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "یادآور دارویاد"
            val descriptionText = "اعلان‌های یادآور نوبت‌های مصرف دارو"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(false)
                setShowBadge(true)
                // صدای آلارم را AlarmSoundManager با تنظیمات کاربر پخش می‌کند؛
                // صدای پیش‌فرض کانال حذف شد تا دو صدا همزمان پخش نشود.
                setSound(null, null)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "med_remind_channel"
    }
}
