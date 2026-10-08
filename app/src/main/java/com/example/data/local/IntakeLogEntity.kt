package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "intake_logs",
    foreignKeys = [
        ForeignKey(
            entity = MedicineEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["medicineId"]), Index(value = ["scheduledTime"])]
)
data class IntakeLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicineId: Long,
    val scheduledTime: Long, // تایم‌استمپ زمان برنامه‌ریزی شده
    val batchHour: Int, // ساعت نوبت رند شده
    val batchMinute: Int, // دقیقه نوبت رند شده
    val takenTime: Long? = null, // زمان مصرف واقعی
    val status: String, // PENDING, TAKEN, SKIPPED
    val logDateString: String // YYYY-MM-DD برای دسته‌بندی راحت در گزارش
)
