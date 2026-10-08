package com.example.util

import java.util.Calendar

data class PersianDate(
    val year: Int,
    val month: Int,
    val day: Int,
    val dayOfWeekName: String,
    val monthName: String,
    val formattedFull: String = "" // مثلاً: چهارشنبه ۱۶ مهر ۱۴۰۵
) {
    fun formatFull(): String = if (formattedFull.isNotBlank()) formattedFull else "$dayOfWeekName، $day $monthName $year"
    fun formatShort(): String = "$day $monthName $year"
}

object PersianCalendarUtil {

    private val persianMonths = listOf(
        "فروردین", "اردیبهشت", "خرداد",
        "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر",
        "دی", "بهمن", "اسفند"
    )

    private val weekDayNames = listOf(
        "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه", "شنبه"
    )

    /**
     * الگوریتم دقیق ریاضی برای تبدیل میلادی به شمسی
     */
    fun gregorianToPersian(gYear: Int, gMonth: Int, gDay: Int, dayOfWeekIndex: Int): PersianDate {
        val gDaysInMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy = gYear - 1600
        val gm = gMonth - 1
        val gd = gDay - 1

        var gDayNo = 365 * gy + ((gy + 3) / 4) - ((gy + 99) / 100) + ((gy + 399) / 400)
        gDayNo += gDaysInMonth[gm] + gd
        if (gm > 1 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))) {
            gDayNo++
        }

        var jDayNo = gDayNo - 79
        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += ((jDayNo - 1) / 365)
            jDayNo = (jDayNo - 1) % 365
        }

        val jm: Int
        val jd: Int
        if (jDayNo < 186) {
            jm = 1 + (jDayNo / 31)
            jd = 1 + (jDayNo % 31)
        } else {
            jm = 7 + ((jDayNo - 186) / 30)
            jd = 1 + ((jDayNo - 186) % 30)
        }

        val dayName = if (dayOfWeekIndex in 1..7) weekDayNames[dayOfWeekIndex - 1] else "امروز"
        val monthName = persianMonths.getOrElse(jm - 1) { "مهر" }
        val formatted = "$dayName $jd $monthName $jy"

        return PersianDate(
            year = jy,
            month = jm,
            day = jd,
            dayOfWeekName = dayName,
            monthName = monthName,
            formattedFull = formatted
        )
    }

    fun getTodayPersianDate(): PersianDate {
        val cal = Calendar.getInstance()
        return gregorianToPersian(
            gYear = cal.get(Calendar.YEAR),
            gMonth = cal.get(Calendar.MONTH) + 1,
            gDay = cal.get(Calendar.DAY_OF_MONTH),
            dayOfWeekIndex = cal.get(Calendar.DAY_OF_WEEK)
        )
    }

    data class PersianDayItem(
        val dayNumber: Int,
        val dayNameShort: String,
        val isToday: Boolean,
        val calendar: Calendar
    )

    fun getCurrentWeekDays(): List<PersianDayItem> {
        val result = mutableListOf<PersianDayItem>()
        val today = Calendar.getInstance()
        val todayDayOfYear = today.get(Calendar.DAY_OF_YEAR)
        val todayYear = today.get(Calendar.YEAR)

        val cal = Calendar.getInstance().apply {
            val currentDow = get(Calendar.DAY_OF_WEEK) // 1=Sunday, 7=Saturday
            val daysFromSaturday = if (currentDow == Calendar.SATURDAY) 0 else currentDow
            add(Calendar.DAY_OF_MONTH, -daysFromSaturday)
        }

        for (i in 0 until 7) {
            val pDate = gregorianToPersian(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.DAY_OF_WEEK)
            )

            val isToday = (cal.get(Calendar.DAY_OF_YEAR) == todayDayOfYear && cal.get(Calendar.YEAR) == todayYear)
            val shortDayName = pDate.dayOfWeekName.take(2)

            result.add(
                PersianDayItem(
                    dayNumber = pDate.day,
                    dayNameShort = shortDayName,
                    isToday = isToday,
                    calendar = cal.clone() as Calendar
                )
            )
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }

        return result
    }
}
