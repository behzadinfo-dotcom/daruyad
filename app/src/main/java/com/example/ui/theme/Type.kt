package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val VazirmatnFontFamily = FontFamily(
    Font(R.font.vazirmatn, FontWeight.Normal),
    Font(R.font.vazirmatn, FontWeight.Medium),
    Font(R.font.vazirmatn, FontWeight.SemiBold),
    Font(R.font.vazirmatn, FontWeight.Bold)
)

fun getResolvedFontWeight(weightLevel: Int, isHeader: Boolean = false): FontWeight {
    return when (weightLevel) {
        700 -> if (isHeader) FontWeight.ExtraBold else FontWeight.Bold
        500 -> if (isHeader) FontWeight.Bold else FontWeight.Medium
        else -> if (isHeader) FontWeight.SemiBold else FontWeight.Normal
    }
}

fun createAppTypography(scale: Float = 1.0f, weightLevel: Int = 400): Typography {
    val bodyWeight = getResolvedFontWeight(weightLevel, isHeader = false)
    val headerWeight = getResolvedFontWeight(weightLevel, isHeader = true)

    return Typography(
        displayLarge = TextStyle(
            fontFamily = VazirmatnFontFamily,
            fontWeight = headerWeight,
            fontSize = (32 * scale).sp,
            lineHeight = (40 * scale).sp
        ),
        displayMedium = TextStyle(
            fontFamily = VazirmatnFontFamily,
            fontWeight = headerWeight,
            fontSize = (26 * scale).sp,
            lineHeight = (34 * scale).sp
        ),
        headlineLarge = TextStyle(
            fontFamily = VazirmatnFontFamily,
            fontWeight = headerWeight,
            fontSize = (22 * scale).sp,
            lineHeight = (30 * scale).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = VazirmatnFontFamily,
            fontWeight = headerWeight,
            fontSize = (19 * scale).sp,
            lineHeight = (26 * scale).sp
        ),
        titleLarge = TextStyle(
            fontFamily = VazirmatnFontFamily,
            fontWeight = headerWeight,
            fontSize = (18 * scale).sp,
            lineHeight = (24 * scale).sp
        ),
        titleMedium = TextStyle(
            fontFamily = VazirmatnFontFamily,
            fontWeight = headerWeight,
            fontSize = (16 * scale).sp,
            lineHeight = (22 * scale).sp
        ),
        titleSmall = TextStyle(
            fontFamily = VazirmatnFontFamily,
            fontWeight = headerWeight,
            fontSize = (14 * scale).sp,
            lineHeight = (20 * scale).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = VazirmatnFontFamily,
            fontWeight = bodyWeight,
            fontSize = (16 * scale).sp,
            lineHeight = (24 * scale).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = VazirmatnFontFamily,
            fontWeight = bodyWeight,
            fontSize = (14 * scale).sp,
            lineHeight = (20 * scale).sp
        ),
        bodySmall = TextStyle(
            fontFamily = VazirmatnFontFamily,
            fontWeight = bodyWeight,
            fontSize = (12 * scale).sp,
            lineHeight = (18 * scale).sp
        ),
        labelLarge = TextStyle(
            fontFamily = VazirmatnFontFamily,
            fontWeight = headerWeight,
            fontSize = (14 * scale).sp,
            lineHeight = (20 * scale).sp
        ),
        labelMedium = TextStyle(
            fontFamily = VazirmatnFontFamily,
            fontWeight = headerWeight,
            fontSize = (12 * scale).sp,
            lineHeight = (16 * scale).sp
        ),
        labelSmall = TextStyle(
            fontFamily = VazirmatnFontFamily,
            fontWeight = bodyWeight,
            fontSize = (11 * scale).sp,
            lineHeight = (14 * scale).sp
        )
    )
}

val Typography = createAppTypography()
