package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PastelAnalogClock(
    modifier: Modifier = Modifier,
    size: Dp = 105.dp,
    dialColor: Color = Color.White,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    accentColor: Color = MaterialTheme.colorScheme.secondary
) {
    var hours by remember { mutableIntStateOf(0) }
    var minutes by remember { mutableIntStateOf(0) }
    var seconds by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            val cal = Calendar.getInstance()
            hours = cal.get(Calendar.HOUR)
            minutes = cal.get(Calendar.MINUTE)
            val sec = cal.get(Calendar.SECOND)
            val millis = cal.get(Calendar.MILLISECOND)
            seconds = sec + millis / 1000f
            delay(100) // حرکت روان
        }
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2, this.size.height / 2)
            val radius = this.size.width / 2 * 0.92f

            // صفحه ساعت با رنگ پاستیلی و کادر ملایم
            drawCircle(
                color = dialColor,
                radius = radius,
                center = center
            )
            drawCircle(
                color = primaryColor.copy(alpha = 0.25f),
                radius = radius,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
            )

            // نشانگرهای ۱۲ ساعته
            for (i in 0 until 12) {
                val angle = Math.toRadians((i * 30 - 90).toDouble())
                val isQuarter = (i % 3 == 0)
                val markerLen = if (isQuarter) 8.dp.toPx() else 4.dp.toPx()
                val markerStroke = if (isQuarter) 2.5.dp.toPx() else 1.5.dp.toPx()
                val markerColor = if (isQuarter) primaryColor else primaryColor.copy(alpha = 0.4f)

                val start = Offset(
                    x = (center.x + (radius - markerLen - 2.dp.toPx()) * cos(angle)).toFloat(),
                    y = (center.y + (radius - markerLen - 2.dp.toPx()) * sin(angle)).toFloat()
                )
                val end = Offset(
                    x = (center.x + (radius - 2.dp.toPx()) * cos(angle)).toFloat(),
                    y = (center.y + (radius - 2.dp.toPx()) * sin(angle)).toFloat()
                )
                drawLine(
                    color = markerColor,
                    start = start,
                    end = end,
                    strokeWidth = markerStroke,
                    cap = StrokeCap.Round
                )
            }

            // عقربه ساعت‌شمار
            val hourAngle = Math.toRadians(((hours % 12 + minutes / 60f) * 30 - 90).toDouble())
            val hourLength = radius * 0.52f
            val hourEnd = Offset(
                x = (center.x + hourLength * cos(hourAngle)).toFloat(),
                y = (center.y + hourLength * sin(hourAngle)).toFloat()
            )
            drawLine(
                color = primaryColor,
                start = center,
                end = hourEnd,
                strokeWidth = 3.8.dp.toPx(),
                cap = StrokeCap.Round
            )

            // عقربه دقیقه‌شمار
            val minuteAngle = Math.toRadians(((minutes + seconds / 60f) * 6 - 90).toDouble())
            val minuteLength = radius * 0.72f
            val minuteEnd = Offset(
                x = (center.x + minuteLength * cos(minuteAngle)).toFloat(),
                y = (center.y + minuteLength * sin(minuteAngle)).toFloat()
            )
            drawLine(
                color = primaryColor.copy(alpha = 0.85f),
                start = center,
                end = minuteEnd,
                strokeWidth = 2.4.dp.toPx(),
                cap = StrokeCap.Round
            )

            // عقربه ثانیه‌شمار نازک و پاستیلی
            val secondAngle = Math.toRadians((seconds * 6 - 90).toDouble())
            val secondLength = radius * 0.82f
            val secondEnd = Offset(
                x = (center.x + secondLength * cos(secondAngle)).toFloat(),
                y = (center.y + secondLength * sin(secondAngle)).toFloat()
            )
            drawLine(
                color = accentColor,
                start = center,
                end = secondEnd,
                strokeWidth = 1.4.dp.toPx(),
                cap = StrokeCap.Round
            )

            // نقطه مرکز ساعت
            drawCircle(
                color = primaryColor,
                radius = 4.dp.toPx(),
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = 1.8.dp.toPx(),
                center = center
            )
        }
    }
}
