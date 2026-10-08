package com.example.util

import java.util.Calendar

object PersianDateHelper {
    fun getTodayPersianDate(calendar: Calendar = Calendar.getInstance()): PersianDate {
        return PersianCalendarUtil.gregorianToPersian(
            gYear = calendar.get(Calendar.YEAR),
            gMonth = calendar.get(Calendar.MONTH) + 1,
            gDay = calendar.get(Calendar.DAY_OF_MONTH),
            dayOfWeekIndex = calendar.get(Calendar.DAY_OF_WEEK)
        )
    }
}
