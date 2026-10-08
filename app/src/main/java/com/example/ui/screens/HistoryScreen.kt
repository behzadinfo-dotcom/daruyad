package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.IntakeLogEntity
import com.example.ui.MainViewModel
import com.example.ui.components.PastelBadge
import com.example.ui.components.PastelCard
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SoftOutline
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningOrange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(viewModel: MainViewModel) {
    val logs by viewModel.recentLogs.collectAsState()
    val allMedicines by viewModel.allMedicines.collectAsState()
    val medicinesMap = allMedicines.associateBy { it.id }

    val takenCount = logs.count { it.status == "TAKEN" }
    val skippedCount = logs.count { it.status == "SKIPPED" }
    val pendingCount = logs.count { it.status == "PENDING" }
    val totalCount = logs.size
    val adherencePercent = if (totalCount > 0) (takenCount * 100) / totalCount else 100

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("history_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "تاریخچه و گزارش مصرف",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "سوابق ثبت شده دوزها و پایبندی دارویی",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // کارت آمار کل و درصد پایبندی
            item {
                PastelCard(
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    cornerRadius = 24.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "درصد پایبندی دارویی",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "$adherencePercent٪",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // سه ستون آمار پاستیلی
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatBox(
                                title = "مصرف شده",
                                count = takenCount,
                                color = SuccessGreen,
                                bg = SuccessGreen.copy(alpha = 0.12f),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            StatBox(
                                title = "رد شده",
                                count = skippedCount,
                                color = ErrorRed,
                                bg = ErrorRed.copy(alpha = 0.12f),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            StatBox(
                                title = "در انتظار",
                                count = pendingCount,
                                color = WarningOrange,
                                bg = WarningOrange.copy(alpha = 0.12f),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "ریز سوابق اخیر",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            if (logs.isEmpty()) {
                item {
                    PastelCard(cornerRadius = 20.dp) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "هنوز سابقه‌ای ثبت نشده است.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        }
                    }
                }
            } else {
                items(logs, key = { it.id }) { log ->
                    val med = medicinesMap[log.medicineId]
                    HistoryLogItem(log = log, medicineName = med?.name ?: "دارو")
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun StatBox(
    title: String,
    count: Int,
    color: Color,
    bg: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun HistoryLogItem(log: IntakeLogEntity, medicineName: String) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())

    val scheduledStr = timeFormat.format(Date(log.scheduledTime))
    val dateStr = log.logDateString

    PastelCard(
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = SoftOutline,
        cornerRadius = 18.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            when (log.status) {
                                "TAKEN" -> SuccessGreen.copy(alpha = 0.15f)
                                "SKIPPED" -> ErrorRed.copy(alpha = 0.15f)
                                else -> WarningOrange.copy(alpha = 0.15f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (log.status) {
                            "TAKEN" -> Icons.Default.CheckCircle
                            "SKIPPED" -> Icons.Default.Close
                            else -> Icons.Default.HourglassEmpty
                        },
                        contentDescription = null,
                        tint = when (log.status) {
                            "TAKEN" -> SuccessGreen
                            "SKIPPED" -> ErrorRed
                            else -> WarningOrange
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = medicineName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "نوبت: $dateStr ساعت $scheduledStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            when (log.status) {
                "TAKEN" -> {
                    val takenAt = log.takenTime?.let { timeFormat.format(Date(it)) } ?: scheduledStr
                    PastelBadge(
                        text = "مصرف در $takenAt",
                        backgroundColor = SuccessGreen.copy(alpha = 0.15f),
                        textColor = SuccessGreen
                    )
                }
                "SKIPPED" -> {
                    PastelBadge(
                        text = "رد شد",
                        backgroundColor = ErrorRed.copy(alpha = 0.15f),
                        textColor = ErrorRed
                    )
                }
                else -> {
                    PastelBadge(
                        text = "در انتظار",
                        backgroundColor = WarningOrange.copy(alpha = 0.15f),
                        textColor = WarningOrange
                    )
                }
            }
        }
    }
}
