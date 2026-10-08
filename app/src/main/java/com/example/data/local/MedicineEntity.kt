package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicines")
data class MedicineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // قرص، کپسول، شربت، آمپول، قطره، پماد، اسپری
    val dosage: String, // مثلاً ۱ عدد، ۵ میلی‌لیتر
    val instructions: String, // قبل غذا، همراه غذا، بعد غذا، با آب فراوان
    val frequencyHours: Int, // فاصله به ساعت (مثلاً 6, 8, 12, 24)
    val firstIntakeHour: Int, // ساعت اولین نوبت (0 تا 23)
    val firstIntakeMinute: Int, // دقیقه اولین نوبت (0 تا 59)
    val firstIntakeTimestamp: Long, // تایم‌استمپ شروع
    val imageUrl: String, // تصویر دارو (آنلاین یا لوکال)
    val imageSourceTag: String, // مثلاً "آنلاین - تأیید شده" یا نام منبع
    val pastelColorKey: String, // mint, lavender, peach, rose, sky, lemon
    val isActive: Boolean = true,
    val isAsNeeded: Boolean = false, // مصرف در مواقع نیاز / لزوم (PRN)
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(defaultValue = "1.0")
    val doseAmount: Double = 1.0, // مقدار هر نوبت (مثلاً ۲)
    @ColumnInfo(defaultValue = "'عدد'")
    val doseUnit: String = "عدد" // واحد مقدار (عدد، قاشق، میلی‌لیتر، پاف، ...)
)
