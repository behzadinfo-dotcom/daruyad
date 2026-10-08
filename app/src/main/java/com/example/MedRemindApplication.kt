package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.alarm.AlarmScheduler
import com.example.data.local.AppDatabase
import com.example.data.local.MedicineEntity
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
            seedInitialMedicinesIfEmpty()
            alarmScheduler.rescheduleAllSmartAlarms()
        }
    }

    private suspend fun seedInitialMedicinesIfEmpty() {
        val existing = database.medicineDao().getActiveMedicinesSync()
        if (existing.isEmpty()) {
            val initialList = listOf(
                MedicineEntity(
                    name = "کپسول امپرازول ۲۰",
                    type = "کپسول",
                    dosage = "۱ عدد ناشتا",
                    instructions = "نیم ساعت قبل از صبحانه با یک لیوان آب",
                    frequencyHours = 24,
                    firstIntakeHour = 8,
                    firstIntakeMinute = 0,
                    firstIntakeTimestamp = System.currentTimeMillis(),
                    imageUrl = "https://images.unsplash.com/photo-1584017911766-d451b3d0e843?w=600&auto=format&fit=crop&q=80",
                    imageSourceTag = "آنلاین - تأیید شده ✓",
                    pastelColorKey = "mint",
                    notes = "برای محافظت از معده",
                    isActive = true
                ),
                MedicineEntity(
                    name = "کپسول آموکسی‌سیلین ۵۰۰",
                    type = "کپسول",
                    dosage = "۱ عدد",
                    instructions = "همراه با صبحانه و آب فراوان",
                    frequencyHours = 8,
                    firstIntakeHour = 8,
                    firstIntakeMinute = 20, // ساعت نزدیک به ۸:۰۰ جهت تست میانگین‌گیری و رند هوشمند
                    firstIntakeTimestamp = System.currentTimeMillis(),
                    imageUrl = "https://images.unsplash.com/photo-1471864190281-a93a3070b6de?w=600&auto=format&fit=crop&q=80",
                    imageSourceTag = "آنلاین - تأیید شده ✓",
                    pastelColorKey = "lavender",
                    notes = "دوره مصرف ۸ روز کامل شود",
                    isActive = true
                ),
                MedicineEntity(
                    name = "قرص استامینوفن ۵۰۰",
                    type = "قرص",
                    dosage = "۱ عدد در صورت نیاز",
                    instructions = "بعد از غذا",
                    frequencyHours = 8,
                    firstIntakeHour = 14,
                    firstIntakeMinute = 0,
                    firstIntakeTimestamp = System.currentTimeMillis(),
                    imageUrl = "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=600&auto=format&fit=crop&q=80",
                    imageSourceTag = "آنلاین - تأیید شده ✓",
                    pastelColorKey = "peach",
                    notes = "مسکن درد",
                    isActive = true
                )
            )
            for (med in initialList) {
                database.medicineDao().insertMedicine(med)
            }
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "یادآور دارویاد"
            val descriptionText = "اعلان‌های زمان‌بندی شده و تجمیعی نوبت‌های مصرف دارو"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "med_remind_channel"
    }
}
