package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CardLemon
import com.example.ui.theme.CardMint
import com.example.ui.theme.CardPeach
import com.example.ui.theme.CardRose
import com.example.ui.theme.CardSky
import com.example.ui.theme.PastelMintPrimary
import com.example.ui.theme.PastelPeachPrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.cos
import kotlin.math.sin

/**
 * رسم نماد بصری دقیق و استاندارد دوز:
 * - ۱۰ میلی لیتر = ۲ قاشق
 * - ۵ میلی لیتر = ۱ قاشق
 * - ۲ پاف = ۲ آیکون پاف
 * - ۱ پاف = ۱ آیکون پاف
 * - کپسول با اپلیکاتور
 * - ۱ قرص = ۱ علامت
 * - ۲ قرص = ۲ علامت
 * - نصف قرص (۱/۲)
 * - یک چهارم قرص (۱/۴)
 * - قطره، آمپول و دوز مواقع نیاز (PRN)
 */
@Composable
fun VisualDosageIcon(
    dosageText: String,
    medicineType: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    pillColor: Color = MaterialTheme.colorScheme.primary
) {
    val normDosage = dosageText.lowercase()
    val normType = medicineType.lowercase()

    // تشخیص دوز
    val isOneQuarter = normDosage.contains("یک چهارم") || normDosage.contains("1/4") || normDosage.contains("ربع")
    val isHalf = normDosage.contains("نصف") || normDosage.contains("نیم") || normDosage.contains("1/2") || normDosage.contains("0.5")
    val isThreePills = normDosage.contains("۳ عدد") || normDosage.contains("3 عدد") || normDosage.contains("۳ قرص") || normDosage.contains("3 قرص")
    val isTwoPills = normDosage.contains("۲ عدد") || normDosage.contains("2 عدد") || normDosage.contains("۲ قرص") || normDosage.contains("2 قرص") || normDosage.contains("دو قرص")

    val isApplicator = normType.contains("اپلیکاتور") || normDosage.contains("اپلیکاتور") || normType.contains("واژینال")
    val isTenMl = normDosage.contains("10 میلی") || normDosage.contains("۱۰ میلی") || normDosage.contains("۱۰ سی") || normDosage.contains("10cc") || normDosage.contains("دو قاشق") || normDosage.contains("۲ قاشق")
    val isFiveMl = normDosage.contains("5 میلی") || normDosage.contains("۵ میلی") || normDosage.contains("۵ سی") || normDosage.contains("5cc") || normDosage.contains("یک قاشق") || normDosage.contains("۱ قاشق") || (normType.contains("شربت") && !isTenMl)

    val isTwoPuffs = normDosage.contains("۲ پاف") || normDosage.contains("2 پاف") || normDosage.contains("دو پاف")
    val isOnePuff = normDosage.contains("۱ پاف") || normDosage.contains("1 پاف") || normDosage.contains("یک پاف") || (normType.contains("اسپری") && !isTwoPuffs)

    val isDrop = normType.contains("قطره")
    val isCapsule = normType.contains("کپسول") && !isApplicator
    val isInjection = normType.contains("آمپول") || normType.contains("تزریق")

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .background(pillColor.copy(alpha = 0.12f))
            .border(1.2.dp, pillColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.78f)) {
            val w = this.size.width
            val h = this.size.height

            when {
                // کپسول دارای اپلیکاتور
                isApplicator -> {
                    // لوله استوانه‌ای اپلیکاتور
                    drawRoundRect(
                        color = pillColor.copy(alpha = 0.5f),
                        topLeft = Offset(w * 0.28f, h * 0.35f),
                        size = Size(w * 0.44f, h * 0.55f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    // پیستون داخلی اپلیکاتور
                    drawRoundRect(
                        color = pillColor,
                        topLeft = Offset(w * 0.42f, h * 0.55f),
                        size = Size(w * 0.16f, h * 0.42f),
                        cornerRadius = CornerRadius(3f, 3f)
                    )
                    // کپسول در سر اپلیکاتور
                    drawRoundRect(
                        color = pillColor,
                        topLeft = Offset(w * 0.3f, h * 0.08f),
                        size = Size(w * 0.4f, h * 0.32f),
                        cornerRadius = CornerRadius(12f, 12f)
                    )
                    // خط میانی کپسول
                    drawLine(
                        color = Color.White,
                        start = Offset(w * 0.3f, h * 0.22f),
                        end = Offset(w * 0.7f, h * 0.22f),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }

                // ۱۰ میلی لیتر = ۲ تا قاشق
                isTenMl -> {
                    // قاشق اول
                    drawSpoon(
                        scope = this,
                        color = pillColor,
                        center = Offset(w * 0.32f, h * 0.5f),
                        scale = 0.85f
                    )
                    // قاشق دوم
                    drawSpoon(
                        scope = this,
                        color = pillColor,
                        center = Offset(w * 0.68f, h * 0.5f),
                        scale = 0.85f
                    )
                }

                // ۵ میلی لیتر = ۱ عدد قاشق
                isFiveMl -> {
                    drawSpoon(
                        scope = this,
                        color = pillColor,
                        center = Offset(w * 0.5f, h * 0.5f),
                        scale = 1.15f
                    )
                }

                // ۲ پاف = ۲ آیکون پاف
                isTwoPuffs -> {
                    drawPuffIcon(scope = this, color = pillColor, offset = Offset(w * 0.05f, h * 0.08f), scale = 0.68f)
                    drawPuffIcon(scope = this, color = pillColor, offset = Offset(w * 0.48f, h * 0.28f), scale = 0.68f)
                }

                // ۱ پاف = ۱ آیکون پاف
                isOnePuff -> {
                    drawPuffIcon(scope = this, color = pillColor, offset = Offset(w * 0.18f, h * 0.18f), scale = 1.0f)
                }

                // یک چهارم قرص (۱/۴)
                isOneQuarter -> {
                    // دایره با یک ربع پررنگ
                    drawCircle(
                        color = pillColor.copy(alpha = 0.2f),
                        radius = w * 0.42f,
                        center = Offset(w * 0.5f, h * 0.5f)
                    )
                    drawArc(
                        color = pillColor,
                        startAngle = 0f,
                        sweepAngle = 90f,
                        useCenter = true,
                        topLeft = Offset(w * 0.08f, h * 0.08f),
                        size = Size(w * 0.84f, h * 0.84f)
                    )
                    // خطوط چهار تکه
                    drawLine(color = Color.White, start = Offset(w * 0.5f, h * 0.08f), end = Offset(w * 0.5f, h * 0.92f), strokeWidth = 1.5.dp.toPx())
                    drawLine(color = Color.White, start = Offset(w * 0.08f, h * 0.5f), end = Offset(w * 0.92f, h * 0.5f), strokeWidth = 1.5.dp.toPx())
                }

                // نصف قرص (۱/۲)
                isHalf -> {
                    drawArc(
                        color = pillColor,
                        startAngle = 90f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(w * 0.08f, h * 0.08f),
                        size = Size(w * 0.84f, h * 0.84f)
                    )
                    drawArc(
                        color = pillColor.copy(alpha = 0.2f),
                        startAngle = 270f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(w * 0.08f, h * 0.08f),
                        size = Size(w * 0.84f, h * 0.84f)
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(w * 0.5f, h * 0.08f),
                        end = Offset(w * 0.5f, h * 0.92f),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // ۲ قرص = ۲ علامت قرص مجزا
                isTwoPills -> {
                    // قرص اول
                    drawCircle(color = pillColor, radius = w * 0.25f, center = Offset(w * 0.32f, h * 0.42f))
                    drawLine(color = Color.White, start = Offset(w * 0.18f, h * 0.42f), end = Offset(w * 0.46f, h * 0.42f), strokeWidth = 1.5.dp.toPx())
                    // قرص دوم
                    drawCircle(color = pillColor, radius = w * 0.25f, center = Offset(w * 0.68f, h * 0.62f))
                    drawLine(color = Color.White, start = Offset(w * 0.54f, h * 0.62f), end = Offset(w * 0.82f, h * 0.62f), strokeWidth = 1.5.dp.toPx())
                }

                // ۳ قرص
                isThreePills -> {
                    drawCircle(color = pillColor, radius = w * 0.2f, center = Offset(w * 0.5f, h * 0.26f))
                    drawCircle(color = pillColor, radius = w * 0.2f, center = Offset(w * 0.28f, h * 0.7f))
                    drawCircle(color = pillColor, radius = w * 0.2f, center = Offset(w * 0.72f, h * 0.7f))
                }

                // کپسول دو رنگ
                isCapsule -> {
                    drawRoundRect(
                        color = pillColor,
                        topLeft = Offset(w * 0.22f, h * 0.12f),
                        size = Size(w * 0.56f, h * 0.38f),
                        cornerRadius = CornerRadius(14f, 14f)
                    )
                    drawRoundRect(
                        color = pillColor.copy(alpha = 0.45f),
                        topLeft = Offset(w * 0.22f, h * 0.48f),
                        size = Size(w * 0.56f, h * 0.38f),
                        cornerRadius = CornerRadius(14f, 14f)
                    )
                }

                // قطره
                isDrop -> {
                    drawCircle(color = pillColor, radius = w * 0.28f, center = Offset(w * 0.5f, h * 0.62f))
                    val path = Path().apply {
                        moveTo(w * 0.5f, h * 0.12f)
                        lineTo(w * 0.75f, h * 0.55f)
                        lineTo(w * 0.25f, h * 0.55f)
                        close()
                    }
                    drawPath(path, color = pillColor)
                }

                // آمپول / تزریق
                isInjection -> {
                    drawRoundRect(
                        color = pillColor,
                        topLeft = Offset(w * 0.35f, h * 0.25f),
                        size = Size(w * 0.3f, h * 0.55f),
                        cornerRadius = CornerRadius(3f, 3f)
                    )
                    drawLine(color = pillColor, start = Offset(w * 0.5f, h * 0.05f), end = Offset(w * 0.5f, h * 0.25f), strokeWidth = 2.dp.toPx())
                    drawLine(color = pillColor, start = Offset(w * 0.5f, h * 0.8f), end = Offset(w * 0.5f, h * 0.98f), strokeWidth = 3.dp.toPx())
                }

                // ۱ قرص = ۱ علامت قرص با شیار دقیق
                else -> {
                    drawCircle(
                        color = pillColor,
                        radius = w * 0.42f,
                        center = Offset(w * 0.5f, h * 0.5f)
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(w * 0.22f, h * 0.5f),
                        end = Offset(w * 0.78f, h * 0.5f),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

/**
 * ترسیم کمکی قاشق شربت
 */
private fun drawSpoon(
    scope: androidx.compose.ui.graphics.drawscope.DrawScope,
    color: Color,
    center: Offset,
    scale: Float = 1.0f
) {
    val r = 10f * scale
    // گودی قاشق (بیضی شکل)
    scope.drawOval(
        color = color,
        topLeft = Offset(center.x - r * 1.5f, center.y - r * 2.2f),
        size = Size(r * 3f, r * 2.8f)
    )
    // مایع داخل قاشق (سفید یا رنگ ملایم)
    scope.drawOval(
        color = Color.White.copy(alpha = 0.85f),
        topLeft = Offset(center.x - r * 0.9f, center.y - r * 1.7f),
        size = Size(r * 1.8f, r * 1.6f)
    )
    // دسته قاشق
    scope.drawLine(
        color = color,
        start = Offset(center.x, center.y + r * 0.6f),
        end = Offset(center.x, center.y + r * 3.4f),
        strokeWidth = 3.5f * scale,
        cap = StrokeCap.Round
    )
}

/**
 * ترسیم کمکی آیکون پاف اسپری
 */
private fun drawPuffIcon(
    scope: androidx.compose.ui.graphics.drawscope.DrawScope,
    color: Color,
    offset: Offset,
    scale: Float = 1.0f
) {
    val w = 24f * scale
    val h = 32f * scale
    // محفظه کپسول اسپری
    scope.drawRoundRect(
        color = color,
        topLeft = offset,
        size = Size(w * 0.65f, h),
        cornerRadius = CornerRadius(4f, 4f)
    )
    // نازل دهانی افقی
    scope.drawRoundRect(
        color = color,
        topLeft = Offset(offset.x + w * 0.4f, offset.y + h * 0.45f),
        size = Size(w * 0.75f, h * 0.4f),
        cornerRadius = CornerRadius(3f, 3f)
    )
    // ابرهای ریز پاف خارج شده
    val puffCenter = Offset(offset.x + w * 1.35f, offset.y + h * 0.55f)
    scope.drawCircle(color = color.copy(alpha = 0.8f), radius = 3.5f * scale, center = puffCenter)
    scope.drawCircle(color = color.copy(alpha = 0.6f), radius = 5.5f * scale, center = Offset(puffCenter.x + 6f * scale, puffCenter.y - 2f * scale))
    scope.drawCircle(color = color.copy(alpha = 0.4f), radius = 4f * scale, center = Offset(puffCenter.x + 10f * scale, puffCenter.y + 3f * scale))
}

/**
 * نماد تصویری و گرافیکی نحوه مصرف:
 * قبل غذا، بعد غذا، همراه غذا، ناشتا، با آب فراوان، قبل خواب، زیر زبانی، موضعی، موقع نیاز (PRN)
 */
@Composable
fun VisualIntakeRuleBadge(
    instructions: String,
    modifier: Modifier = Modifier,
    isLarge: Boolean = false
) {
    val iconSize = if (isLarge) 24.dp else 16.dp
    val textStyle = if (isLarge) MaterialTheme.typography.titleSmall else MaterialTheme.typography.labelSmall

    val (label, icon, bg, tint) = when {
        instructions.contains("مواقع نیاز") || instructions.contains("موقع نیاز") || instructions.contains("در صورت نیاز") || instructions.contains("هنگام درد") || instructions.contains("PRN") -> {
            Tuple4("مواقع نیاز (درد/تب)", Icons.Default.HealthAndSafety, CardRose, Color(0xFFE11D48))
        }
        instructions.contains("ناشتا") -> {
            Tuple4("ناشتا (با معده خالی)", Icons.Default.WbSunny, CardLemon, Color(0xFFD97706))
        }
        instructions.contains("قبل") && instructions.contains("غذا") -> {
            Tuple4("قبل از غذا", Icons.Outlined.Restaurant, CardPeach, Color(0xFFEA580C))
        }
        instructions.contains("بعد") && instructions.contains("غذا") -> {
            Tuple4("بعد از غذا", Icons.Default.Restaurant, CardMint, Color(0xFF16A34A))
        }
        instructions.contains("همراه") || instructions.contains("وسط") -> {
            Tuple4("همراه با غذا", Icons.Default.Fastfood, CardPeach, Color(0xFFCA8A04))
        }
        instructions.contains("آب") -> {
            Tuple4("با آب فراوان", Icons.Default.LocalDrink, CardSky, Color(0xFF0284C7))
        }
        instructions.contains("خواب") || instructions.contains("شب") -> {
            Tuple4("قبل از خواب", Icons.Default.Bedtime, Color(0xFFEDE9FE), Color(0xFF7C3AED))
        }
        instructions.contains("زیر زبان") -> {
            Tuple4("زیر زبانی", Icons.Default.Favorite, CardRose, Color(0xFFBE185D))
        }
        instructions.contains("موضعی") || instructions.contains("مالش") || instructions.contains("پوستی") -> {
            Tuple4("موضعی / مالیدنی", Icons.Default.PanTool, CardSky, Color(0xFF0369A1))
        }
        instructions.contains("اپلیکاتور") || instructions.contains("واژینال") -> {
            Tuple4("با اپلیکاتور", Icons.Default.MedicalServices, Color(0xFFF3E8FF), Color(0xFF9333EA))
        }
        else -> {
            Tuple4(instructions.ifBlank { "طبق دستور پزشک" }, Icons.Default.Schedule, CardMint, MaterialTheme.colorScheme.primary)
        }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = bg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (isLarge) 12.dp else 8.dp, vertical = if (isLarge) 8.dp else 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = textStyle,
                fontWeight = FontWeight.Bold,
                color = tint
            )
        }
    }
}

/**
 * نماد زمان روز (صبح، ظهر، عصر، شب)
 */
@Composable
fun VisualTimeOfDayBadge(
    hour: Int,
    modifier: Modifier = Modifier
) {
    val (label, icon, color, bg) = when (hour) {
        in 5..11 -> Tuple4("صبح", Icons.Default.WbSunny, Color(0xFFD97706), CardLemon)
        in 12..16 -> Tuple4("ظهر", Icons.Default.Brightness5, Color(0xFFEA580C), CardPeach)
        in 17..19 -> Tuple4("عصر", Icons.Default.WbSunny, Color(0xFFE11D48), Color(0xFFFFE4E6))
        else -> Tuple4("شب", Icons.Default.Bedtime, Color(0xFF7C3AED), Color(0xFFEDE9FE))
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = bg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

/**
 * انیمیشن تعاملی و آموزشی بسیار جذاب Canvas برای مصارف خاص:
 * - اپلیکاتور (حرکت پیستون و هدایت کپسول)
 * - اسپری تنفسی (فشرده شدن نازل و خروج ابرهای متحرک پاف)
 * - شربت و قاشق (ریختن قطرات شربت در قاشق)
 * - قطره‌چکان (چکیدن مداوم قطره)
 * - قرص جوشان در آب (حباب‌های متحرک)
 */
@Composable
fun SpecialIntakeAnimatedCanvas(
    medicineType: String,
    dosageText: String,
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SpecialIntakeAnimation")
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    val normType = medicineType.lowercase()
    val isApplicator = normType.contains("اپلیکاتور") || dosageText.contains("اپلیکاتور") || normType.contains("واژینال")
    val isSpray = normType.contains("اسپری") || dosageText.contains("پاف")
    val isSyrup = normType.contains("شربت") || dosageText.contains("قاشق") || dosageText.contains("میلی")
    val isDrop = normType.contains("قطره")
    val isEffervescent = normType.contains("جوشان")

    Box(
        modifier = modifier
            .size(110.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(primaryColor.copy(alpha = 0.08f))
            .border(1.5.dp, primaryColor.copy(alpha = 0.25f), RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(90.dp)) {
            val w = size.width
            val h = size.height

            when {
                // ۱. انیمیشن متحرک اپلیکاتور (هدایت کپسول با پیستون)
                isApplicator -> {
                    val pistonOffset = (sin(animationProgress * Math.PI.toFloat()) * (h * 0.22f)).coerceAtLeast(0f)

                    // بدنه شیشه‌ای / شفاف اپلیکاتور
                    drawRoundRect(
                        color = primaryColor.copy(alpha = 0.35f),
                        topLeft = Offset(w * 0.32f, h * 0.25f),
                        size = Size(w * 0.36f, h * 0.5f),
                        cornerRadius = CornerRadius(6f, 6f),
                        style = Stroke(width = 2.5.dp.toPx())
                    )

                    // پیستون در حال فشرده شدن
                    drawRoundRect(
                        color = primaryColor,
                        topLeft = Offset(w * 0.42f, h * 0.55f - pistonOffset),
                        size = Size(w * 0.16f, h * 0.38f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    // دسته پیستون
                    drawLine(
                        color = primaryColor,
                        start = Offset(w * 0.32f, h * 0.93f - pistonOffset),
                        end = Offset(w * 0.68f, h * 0.93f - pistonOffset),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // کپسول در حال خروج هدایت شده
                    val capsuleY = (h * 0.18f - pistonOffset * 0.8f).coerceAtLeast(h * 0.04f)
                    drawRoundRect(
                        color = primaryColor,
                        topLeft = Offset(w * 0.34f, capsuleY),
                        size = Size(w * 0.32f, h * 0.24f),
                        cornerRadius = CornerRadius(14f, 14f)
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(w * 0.34f, capsuleY + h * 0.12f),
                        end = Offset(w * 0.66f, capsuleY + h * 0.12f),
                        strokeWidth = 1.8.dp.toPx()
                    )
                }

                // ۲. انیمیشن اسپری تنفسی (خروج ابرهای متحرک پاف)
                isSpray -> {
                    val pressY = if (animationProgress in 0.3f..0.7f) 4.dp.toPx() else 0f

                    // بدنه اسپری
                    drawRoundRect(
                        color = primaryColor,
                        topLeft = Offset(w * 0.15f, h * 0.25f + pressY),
                        size = Size(w * 0.35f, h * 0.65f),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                    // نازل دهانی
                    drawRoundRect(
                        color = primaryColor,
                        topLeft = Offset(w * 0.35f, h * 0.6f + pressY),
                        size = Size(w * 0.3f, h * 0.25f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )

                    // خروج ابرهای پاف متحرک
                    val puffAlpha = (1f - animationProgress).coerceIn(0f, 1f)
                    val puffDistance = animationProgress * (w * 0.4f)
                    val puffCenter = Offset(w * 0.65f + puffDistance, h * 0.72f)

                    drawCircle(color = primaryColor.copy(alpha = puffAlpha * 0.8f), radius = w * 0.07f * (1f + animationProgress * 0.5f), center = puffCenter)
                    drawCircle(color = primaryColor.copy(alpha = puffAlpha * 0.6f), radius = w * 0.11f * (1f + animationProgress * 0.5f), center = Offset(puffCenter.x + 8f, puffCenter.y - 6f))
                    drawCircle(color = primaryColor.copy(alpha = puffAlpha * 0.4f), radius = w * 0.09f * (1f + animationProgress * 0.5f), center = Offset(puffCenter.x + 14f, puffCenter.y + 7f))
                }

                // ۳. انیمیشن قاشق شربت (پر شدن و ریختن قطره)
                isSyrup -> {
                    // قاشق
                    drawSpoon(scope = this, color = primaryColor, center = Offset(w * 0.5f, h * 0.62f), scale = 1.3f)

                    // قطره شربت در حال سقوط از بالا به قاشق
                    val dropY = h * 0.1f + (animationProgress * (h * 0.45f))
                    val dropAlpha = if (animationProgress > 0.85f) 0f else 1f
                    drawCircle(
                        color = primaryColor.copy(alpha = dropAlpha),
                        radius = 4.dp.toPx(),
                        center = Offset(w * 0.5f, dropY)
                    )
                }

                // ۴. قطره‌چکان (چکیدن مداوم قطره براق)
                isDrop -> {
                    // نوک قطره‌چکان
                    drawRoundRect(
                        color = primaryColor.copy(alpha = 0.5f),
                        topLeft = Offset(w * 0.42f, h * 0.08f),
                        size = Size(w * 0.16f, h * 0.35f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    // قطره در حال چکیدن
                    val dropY = h * 0.42f + (animationProgress * (h * 0.48f))
                    val dropRadius = (w * 0.09f * (1f - animationProgress * 0.3f)).coerceAtLeast(2f)
                    drawCircle(
                        color = primaryColor,
                        radius = dropRadius,
                        center = Offset(w * 0.5f, dropY)
                    )
                    // حلقه امواج مایع در پایین
                    if (animationProgress > 0.6f) {
                        val waveRadius = (animationProgress - 0.6f) * (w * 0.4f)
                        val waveAlpha = 1f - (animationProgress - 0.6f) / 0.4f
                        drawCircle(
                            color = primaryColor.copy(alpha = waveAlpha * 0.5f),
                            radius = waveRadius,
                            center = Offset(w * 0.5f, h * 0.88f),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }

                // ۵. قرص جوشان در آب (حباب‌های گاز در حال صعود)
                isEffervescent -> {
                    // لیوان
                    drawRoundRect(
                        color = primaryColor.copy(alpha = 0.3f),
                        topLeft = Offset(w * 0.25f, h * 0.15f),
                        size = Size(w * 0.5f, h * 0.75f),
                        cornerRadius = CornerRadius(8f, 8f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    // قرص در ته لیوان
                    drawRoundRect(
                        color = primaryColor,
                        topLeft = Offset(w * 0.33f, h * 0.76f),
                        size = Size(w * 0.34f, h * 0.1f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    // حباب‌های صعودی
                    val bubble1Y = h * 0.72f - ((animationProgress * 1.2f) % 1f) * (h * 0.55f)
                    val bubble2Y = h * 0.72f - (((animationProgress + 0.4f) * 1.2f) % 1f) * (h * 0.55f)
                    val bubble3Y = h * 0.72f - (((animationProgress + 0.7f) * 1.2f) % 1f) * (h * 0.55f)

                    drawCircle(color = primaryColor.copy(alpha = 0.7f), radius = 3.dp.toPx(), center = Offset(w * 0.42f, bubble1Y))
                    drawCircle(color = primaryColor.copy(alpha = 0.6f), radius = 4.dp.toPx(), center = Offset(w * 0.55f, bubble2Y))
                    drawCircle(color = primaryColor.copy(alpha = 0.8f), radius = 2.5.dp.toPx(), center = Offset(w * 0.48f, bubble3Y))
                }

                // حالت پیش‌فرض: پالس ملایم قرص
                else -> {
                    val pulseScale = 1f + sin(animationProgress * Math.PI.toFloat() * 2) * 0.08f
                    drawCircle(
                        color = primaryColor,
                        radius = w * 0.36f * pulseScale,
                        center = Offset(w * 0.5f, h * 0.5f)
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(w * 0.24f, h * 0.5f),
                        end = Offset(w * 0.76f, h * 0.5f),
                        strokeWidth = 2.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

/**
 * ردیف نمادهای بصری و کاملاً تصویری برای اشخاص بی‌سواد، کم‌سواد و سالمندان
 */
@Composable
fun IlliterateMedicineVisualRow(
    dosageText: String,
    medicineType: String,
    instructions: String,
    hour: Int,
    modifier: Modifier = Modifier,
    isExtraLarge: Boolean = false
) {
    val iconSize = if (isExtraLarge) 58.dp else 46.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.95f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ۱. نماد گرافیکی تعداد و نوع دوز (قرص، نصف، یک چهارم، قاشق‌ها، پاف‌ها)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            VisualDosageIcon(
                dosageText = dosageText,
                medicineType = medicineType,
                size = iconSize
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dosageText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        // ۲. نماد گرافیکی دستور مصرف (غذا، آب فراوان، ناشتا، موقع نیاز)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            VisualIntakeRuleBadge(
                instructions = instructions,
                isLarge = isExtraLarge
            )
        }

        // ۳. نماد زمان شبانه‌روز (صبح، ظهر، عصر، شب)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            VisualTimeOfDayBadge(hour = hour)
        }
    }
}
