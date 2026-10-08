package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CardMint
import com.example.ui.theme.PastelRosePrimary
import com.example.ui.theme.SoftOutline
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.PersianCalendarUtil
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * ساعت آنالوگ مدرن و پاستیلی با عقربه‌های متحرک زنده
 */
@Composable
fun PastelAnalogClock(
    modifier: Modifier = Modifier,
    size: Dp = 110.dp,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    accentColor: Color = PastelRosePrimary
) {
    // به‌روزرسانی زمان هر ۱ ثانیه
    val currentTime by produceState(initialValue = Calendar.getInstance()) {
        while (true) {
            value = Calendar.getInstance()
            delay(1000)
        }
    }

    val hours = currentTime.get(Calendar.HOUR)
    val minutes = currentTime.get(Calendar.MINUTE)
    val seconds = currentTime.get(Calendar.SECOND)

    // محاسبه زوایا (بر حسب رادیان)
    val secondAngle = (seconds * 6.0) - 90.0
    val minuteAngle = ((minutes + seconds / 60.0) * 6.0) - 90.0
    val hourAngle = (((hours % 12) + minutes / 60.0) * 30.0) - 90.0

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                )
            )
            .border(2.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size - 12.dp)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = this.size.width / 2f

            // ۱. رسم نشانگرهای ۱۲ ساعته
            for (i in 0 until 12) {
                val tickAngle = Math.toRadians(i * 30.0 - 90.0)
                val isQuarter = (i % 3 == 0)
                val innerR = if (isQuarter) radius * 0.75f else radius * 0.85f
                val outerR = radius * 0.92f

                val start = Offset(
                    x = (center.x + innerR * cos(tickAngle)).toFloat(),
                    y = (center.y + innerR * sin(tickAngle)).toFloat()
                )
                val end = Offset(
                    x = (center.x + outerR * cos(tickAngle)).toFloat(),
                    y = (center.y + outerR * sin(tickAngle)).toFloat()
                )

                drawLine(
                    color = if (isQuarter) primaryColor else SoftOutline,
                    start = start,
                    end = end,
                    strokeWidth = if (isQuarter) 3f else 1.5f,
                    cap = StrokeCap.Round
                )
            }

            // ۲. عقربه ساعت‌شمار
            val hourRad = Math.toRadians(hourAngle)
            val hourLength = radius * 0.50f
            val hourEnd = Offset(
                x = (center.x + hourLength * cos(hourRad)).toFloat(),
                y = (center.y + hourLength * sin(hourRad)).toFloat()
            )
            drawLine(
                color = TextPrimary,
                start = center,
                end = hourEnd,
                strokeWidth = 4.5f,
                cap = StrokeCap.Round
            )

            // ۳. عقربه دقیقه‌شمار
            val minRad = Math.toRadians(minuteAngle)
            val minLength = radius * 0.72f
            val minEnd = Offset(
                x = (center.x + minLength * cos(minRad)).toFloat(),
                y = (center.y + minLength * sin(minRad)).toFloat()
            )
            drawLine(
                color = primaryColor,
                start = center,
                end = minEnd,
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )

            // ۴. عقربه ثانیه‌شمار
            val secRad = Math.toRadians(secondAngle)
            val secLength = radius * 0.82f
            val secEnd = Offset(
                x = (center.x + secLength * cos(secRad)).toFloat(),
                y = (center.y + secLength * sin(secRad)).toFloat()
            )
            drawLine(
                color = accentColor,
                start = center,
                end = secEnd,
                strokeWidth = 1.8f,
                cap = StrokeCap.Round
            )

            // ۵. دایره مرکزی
            drawCircle(
                color = accentColor,
                radius = 4f,
                center = center
            )
        }
    }
}

/**
 * ویجت تقویم شمسی و ساعت آنالوگ ترکیبی در صفحه اصلی
 */
@Composable
fun AnalogClockAndPersianCalendarWidget(
    modifier: Modifier = Modifier
) {
    val persianToday = remember { PersianCalendarUtil.getTodayPersianDate() }
    val weekDays = remember { PersianCalendarUtil.getCurrentWeekDays() }

    val currentTimeString by produceState(initialValue = "") {
        while (true) {
            val cal = Calendar.getInstance()
            value = String.format(Locale.getDefault(), "%02d:%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND))
            delay(1000)
        }
    }

    PastelCard(
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = SoftOutline,
        cornerRadius = 24.dp,
        modifier = modifier
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ردیف هدر: ساعت آنالوگ در کنار تاریخ شمسی
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "تقویم خورشیدی و زمان",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = persianToday.formattedFull,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "زمان زنده: $currentTimeString",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // ساعت آنالوگ زیبا
                PastelAnalogClock(
                    size = 96.dp,
                    primaryColor = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // نوار روزهای هفته شمسی
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekDays.forEach { dayItem ->
                    val isToday = dayItem.isToday
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 2.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isToday) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            )
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = dayItem.dayNameShort,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isToday) Color.White.copy(alpha = 0.9f) else TextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${dayItem.dayNumber}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (isToday) Color.White else TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
