package com.example.data.repository

import com.example.data.local.IntakeLogDao
import com.example.data.local.IntakeLogEntity
import com.example.data.local.MedicineDao
import com.example.data.local.MedicineEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

// نمای مصور یک داروی حاضر در نوبت مشخص
data class MedicineDoseItem(
    val medicine: MedicineEntity,
    val scheduledTime: Long,
    val intakeLogId: Long,
    val status: String // PENDING, TAKEN, SKIPPED
)

// نمای مصور یک نوبت تجمیعی (ساعت رند شده با لیست داروهای تجمیع شده)
data class ClusteredIntakeSession(
    val batchHour: Int,
    val batchMinute: Int,
    val formattedTime: String, // مثلاً "۰۸:۱۵ صبح"
    val dateString: String,
    val doseItems: List<MedicineDoseItem>,
    val originalDoseTimes: List<Pair<String, String>>, // اسامی داروها و ساعت دقیق قبل از رند کردن
    val isAllTaken: Boolean,
    val isPending: Boolean
)

class MedicineRepository(
    private val medicineDao: MedicineDao,
    private val intakeLogDao: IntakeLogDao
) {

    fun getAllMedicines(): Flow<List<MedicineEntity>> = medicineDao.getAllMedicines()

    fun getActiveMedicines(): Flow<List<MedicineEntity>> = medicineDao.getActiveMedicines()

    suspend fun getMedicineById(id: Long): MedicineEntity? = medicineDao.getMedicineById(id)

    suspend fun insertMedicine(medicine: MedicineEntity): Long = medicineDao.insertMedicine(medicine)

    suspend fun updateMedicine(medicine: MedicineEntity) = medicineDao.updateMedicine(medicine)

    suspend fun deleteMedicine(medicine: MedicineEntity) = medicineDao.deleteMedicine(medicine)

    suspend fun toggleMedicineActive(id: Long, isActive: Boolean) = medicineDao.setMedicineActive(id, isActive)

    fun getLogsForDate(dateString: String): Flow<List<IntakeLogEntity>> = intakeLogDao.getLogsForDate(dateString)

    fun getRecentLogs(): Flow<List<IntakeLogEntity>> = intakeLogDao.getRecentLogs()

    suspend fun updateLogStatus(logId: Long, status: String) {
        val takenTime = if (status == "TAKEN") System.currentTimeMillis() else null
        intakeLogDao.updateLogStatus(logId, status, takenTime)
    }

    suspend fun recordManualIntake(medicineId: Long, status: String = "TAKEN") {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
        val log = IntakeLogEntity(
            medicineId = medicineId,
            scheduledTime = now,
            batchHour = cal.get(Calendar.HOUR_OF_DAY),
            batchMinute = cal.get(Calendar.MINUTE),
            status = status,
            takenTime = now,
            logDateString = dateStr
        )
        intakeLogDao.insertOrUpdateLog(log)
    }

    suspend fun markAllInSession(dateString: String, hour: Int, minute: Int, status: String) {
        val takenTime = if (status == "TAKEN") System.currentTimeMillis() else null
        intakeLogDao.markAllInBatch(dateString, hour, minute, status, takenTime)
    }

    /**
     * الگوریتم هوشمند خوشه‌بندی، میانگین‌گیری و رند کردن آلارم‌ها برای تاریخ مشخص:
     * 1. تمام زمان‌های مصرف داروهای فعال را در ۲۴ ساعت روز محاسبه می‌کند.
     * 2. نوبت‌های نزدیک (کمتر از 40 دقیقه فاصله) را خوشه‌بندی می‌کند.
     * 3. میانگین ساعت هر خوشه را محاسبه کرده و به ضریب 15 دقیقه (یا رند) گرد می‌کند.
     * 4. برای هر نوبت سوابق IntakeLog را ایجاد یا همگام می‌کند و جلسات را برمی‌گرداند.
     */
    suspend fun generateDailySessions(date: Calendar = Calendar.getInstance()): List<ClusteredIntakeSession> {
        val activeMeds = medicineDao.getActiveMedicinesSync().filter { !it.isAsNeeded }
        if (activeMeds.isEmpty()) return emptyList()

        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date.time)

        // ساخت تمام زمان‌های مصرف پیش‌بینی شده در این روز بر حسب دقیقه از نیمه‌شب (0 تا 1439)
        data class RawDose(val medicine: MedicineEntity, val minuteOfDay: Int, val exactTimestamp: Long)
        val rawDoses = mutableListOf<RawDose>()

        val dayCal = Calendar.getInstance().apply {
            time = date.time
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        for (med in activeMeds) {
            val freqHours = if (med.frequencyHours > 0) med.frequencyHours else 24
            var currentMinuteOfDay = med.firstIntakeHour * 60 + med.firstIntakeMinute

            // اطمینان از اینکه زمان در بازه 0 تا 1439 تنظیم شود
            currentMinuteOfDay %= 1440

            var step = 0
            while (step < 24 / freqHours.coerceAtLeast(1) && step < 12) {
                val doseMinute = (currentMinuteOfDay + step * freqHours * 60) % 1440
                val doseCal = (dayCal.clone() as Calendar).apply {
                    add(Calendar.MINUTE, doseMinute)
                }

                rawDoses.add(RawDose(med, doseMinute, doseCal.timeInMillis))
                step++
            }
        }

        // مرتب‌سازی بر اساس دقیقه روز
        rawDoses.sortBy { it.minuteOfDay }

        // الگوریتم کلاسترینگ نوبت‌های نزدیک به هم (فاصله کمتر از 40 دقیقه)
        val clusters = mutableListOf<MutableList<RawDose>>()
        var currentCluster = mutableListOf<RawDose>()

        for (dose in rawDoses) {
            if (currentCluster.isEmpty()) {
                currentCluster.add(dose)
            } else {
                val prev = currentCluster.last()
                val diff = dose.minuteOfDay - prev.minuteOfDay
                if (diff in 0..40) {
                    currentCluster.add(dose)
                } else {
                    clusters.add(currentCluster)
                    currentCluster = mutableListOf(dose)
                }
            }
        }
        if (currentCluster.isNotEmpty()) {
            clusters.add(currentCluster)
        }

        // پردازش هر کلاستر: میانگین‌گیری و رند کردن به مضرب 15 دقیقه
        val sessions = mutableListOf<ClusteredIntakeSession>()

        for (cluster in clusters) {
            // محاسبه میانگین دقایق
            val avgMinuteOfDay = cluster.map { it.minuteOfDay }.average().roundToInt()

            // رند کردن میانگین به مضرب 15 دقیقه (مثلاً 00, 15, 30, 45)
            val roundedMinuteOfDay = ((avgMinuteOfDay + 7) / 15) * 15 % 1440
            val batchHour = roundedMinuteOfDay / 60
            val batchMinute = roundedMinuteOfDay % 60

            val doseItems = mutableListOf<MedicineDoseItem>()
            val originalTimes = mutableListOf<Pair<String, String>>()

            for (raw in cluster) {
                val origH = raw.minuteOfDay / 60
                val origM = raw.minuteOfDay % 60
                originalTimes.add(Pair(raw.medicine.name, String.format(Locale.getDefault(), "%02d:%02d", origH, origM)))

                // بررسی وجود لاگ در دیتابیس
                var existingLog = intakeLogDao.findLog(raw.medicine.id, raw.exactTimestamp)
                val logId = if (existingLog == null) {
                    val newLog = IntakeLogEntity(
                        medicineId = raw.medicine.id,
                        scheduledTime = raw.exactTimestamp,
                        batchHour = batchHour,
                        batchMinute = batchMinute,
                        takenTime = null,
                        status = "PENDING",
                        logDateString = dateStr
                    )
                    intakeLogDao.insertOrUpdateLog(newLog)
                } else {
                    existingLog.id
                }

                val currentStatus = existingLog?.status ?: "PENDING"
                doseItems.add(
                    MedicineDoseItem(
                        medicine = raw.medicine,
                        scheduledTime = raw.exactTimestamp,
                        intakeLogId = logId,
                        status = currentStatus
                    )
                )
            }

            val isAllTaken = doseItems.isNotEmpty() && doseItems.all { it.status == "TAKEN" }
            val isPending = doseItems.any { it.status == "PENDING" }

            val session = ClusteredIntakeSession(
                batchHour = batchHour,
                batchMinute = batchMinute,
                formattedTime = formatHourMinutePersian(batchHour, batchMinute),
                dateString = dateStr,
                doseItems = doseItems,
                originalDoseTimes = originalTimes,
                isAllTaken = isAllTaken,
                isPending = isPending
            )
            sessions.add(session)
        }

        return sessions
    }

    suspend fun logAsNeededIntake(medicine: MedicineEntity) {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val log = IntakeLogEntity(
            medicineId = medicine.id,
            scheduledTime = now,
            batchHour = hour,
            batchMinute = minute,
            takenTime = now,
            status = "TAKEN",
            logDateString = dateStr
        )
        intakeLogDao.insertOrUpdateLog(log)
    }

    private fun formatHourMinutePersian(hour: Int, minute: Int): String {
        val period = if (hour < 12) "صبح" else if (hour < 17) "ظهر" else if (hour < 20) "عصر" else "شب"
        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        return String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, minute, period)
    }
}
