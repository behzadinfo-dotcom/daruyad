package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.roundToInt

class AlarmClusteringAndRoundingTest {

    @Test
    fun testClusteringCloseTimesAndRounding() {
        // فرض کنید دو نوبت داریم: یکی ۰۸:۱۰ (۴۹۰ دقیقه از بامداد) و دیگری ۰۸:۲۰ (۵۰۰ دقیقه از بامداد)
        val dose1Minutes = 8 * 60 + 10 // 490
        val dose2Minutes = 8 * 60 + 20 // 500

        val diff = dose2Minutes - dose1Minutes
        assertTrue("فاصله باید کمتر از ۴۰ دقیقه باشد تا کلاستر شود", diff <= 40)

        // میانگین دقایق
        val avgMinutes = listOf(dose1Minutes, dose2Minutes).average().roundToInt() // 495 (ساعت ۰۸:۱۵)
        assertEquals(495, avgMinutes)

        // رند کردن به مضرب ۱۵ دقیقه
        val roundedMinutes = ((avgMinutes + 7) / 15) * 15 % 1440
        assertEquals(495, roundedMinutes)

        val batchHour = roundedMinutes / 60
        val batchMinute = roundedMinutes % 60
        assertEquals(8, batchHour)
        assertEquals(15, batchMinute)
    }

    @Test
    fun testRoundingBoundaryTimes() {
        // نوبت ۰۸:۰۴ باید به ۰۸:۰۰ رند شود
        val m1 = 8 * 60 + 4
        val r1 = ((m1 + 7) / 15) * 15
        assertEquals(8 * 60, r1)

        // نوبت ۰۸:۱۱ باید به ۰۸:۱۵ رند شود
        val m2 = 8 * 60 + 11
        val r2 = ((m2 + 7) / 15) * 15
        assertEquals(8 * 60 + 15, r2)
    }
}
