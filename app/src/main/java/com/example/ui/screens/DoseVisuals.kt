package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * نشانهٔ وضعیت دوز با رنگ، حرکت و شکل (بدون نیاز به خواندن):
 * - در انتظار: دایرهٔ نارنجی با نماد دست که آرام می‌تپد؛ لمس آن دوز را ثبت می‌کند
 * - خورده شده: دایرهٔ سبز با تیک که با انیمیشن ظاهر می‌شود
 * - رد شده: دایرهٔ خاکستری با ضربدر
 */
@Composable
fun DoseStatusOrb(
    status: String,
    hour: Int,
    onTap: (() -> Unit)?,
    modifier: Modifier = Modifier.fillMaxWidth(),
    orbSize: Dp = 104.dp,
    iconSize: Dp = 60.dp,
    showTime: Boolean = true
) {
    val isPending = status == "PENDING"
    val isTaken = status == "TAKEN"

    val (orbColor, icon) = when (status) {
        "TAKEN" -> Color(0xFF2E9E5B) to (Icons.Filled.CheckCircle as ImageVector)
        "SKIPPED" -> Color(0xFF8E8E93) to Icons.Filled.Close
        else -> Color(0xFFF2A531) to Icons.Filled.PanTool
    }

    val infinite = rememberInfiniteTransition(label = "orb_pulse")
    val pulse by infinite.animateFloat(
        initialValue = 1f,
        targetValue = if (isPending) 1.10f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_pulse_value"
    )

    val popScale by animateFloatAsState(
        targetValue = if (isTaken) 1f else 0.85f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "orb_pop"
    )

    val timeIcon: ImageVector = when {
        hour < 12 -> Icons.Filled.WbSunny
        hour < 18 -> Icons.Filled.Brightness5
        else -> Icons.Filled.Bedtime
    }
    val timeLabel = when {
        hour < 12 -> "صبح"
        hour < 18 -> "ظهر و عصر"
        else -> "شب"
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(orbSize)
                .scale(if (isPending) pulse else popScale)
                .clip(CircleShape)
                .background(orbColor)
                .clickable(enabled = onTap != null) { onTap?.invoke() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(iconSize)
            )
        }

        if (showTime) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = timeIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = timeLabel,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
