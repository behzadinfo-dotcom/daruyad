package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.repository.ClusteredIntakeSession
import com.example.data.repository.MedicineDoseItem
import com.example.ui.MainViewModel
import com.example.ui.components.IlliterateMedicineVisualRow
import com.example.ui.components.PastelActionButton
import com.example.ui.components.PastelBadge
import com.example.ui.components.PastelCard
import com.example.ui.components.PastelSkipButton
import com.example.ui.components.VisualDosageIcon
import com.example.ui.components.VisualIntakeRuleBadge
import com.example.ui.components.VisualTimeOfDayBadge
import com.example.ui.components.getMedicineTypeIcon
import com.example.ui.components.getPastelColorByKey
import com.example.ui.theme.CardMint
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SoftOutline
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    batchHour: Int,
    batchMinute: Int,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val sessions by viewModel.dailySessions.collectAsState()
    val session = sessions.firstOrNull { it.batchHour == batchHour && it.batchMinute == batchMinute }
        ?: sessions.firstOrNull() // فال‌بک به اولین نوبت در صورت پیدا نشدن

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "نوبت مصور مصرف دارو",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = session?.formattedTime ?: "ساعت ${String.format("%02d:%02d", batchHour, batchMinute)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("session_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                modifier = Modifier.statusBarsPadding()
            )
        },
        bottomBar = {
            if (session != null && session.isPending) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Button(
                            onClick = {
                                viewModel.markAllInSession(session, "TAKEN")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("take_all_session_button"),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ثبت مصرف همه داروهای این ساعت",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (session == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "اطلاعات این نوبت یافت نشد یا تغییر کرده است.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // کارت هدر نوبت تجمیعی و توضیح میانگین‌گیری
                item {
                    SessionBatchHeaderCard(session)
                }

                // لیست آیتم‌های مصور داروها
                items(session.doseItems, key = { it.intakeLogId }) { doseItem ->
                    val userSettings by viewModel.userSettings.collectAsState()
                    PictorialMedicineCard(
                        doseItem = doseItem,
                        batchHour = session.batchHour,
                        isVisualMode = userSettings.isVisualAccessibilityMode,
                        onMarkTaken = { viewModel.markDoseStatus(doseItem.intakeLogId, "TAKEN") },
                        onMarkSkipped = { viewModel.markDoseStatus(doseItem.intakeLogId, "SKIPPED") }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun SessionBatchHeaderCard(session: ClusteredIntakeSession) {
    PastelCard(
        backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
        cornerRadius = 24.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = session.formattedTime,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "نوبت تجمیع شده (${session.doseItems.size} دارو)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                if (session.isAllTaken) {
                    PastelBadge(
                        text = "مصرف شده ✓",
                        backgroundColor = SuccessGreen.copy(alpha = 0.2f),
                        textColor = SuccessGreen,
                        icon = Icons.Default.CheckCircle
                    )
                } else {
                    PastelBadge(
                        text = "در انتظار مصرف",
                        backgroundColor = WarningOrange.copy(alpha = 0.2f),
                        textColor = WarningOrange
                    )
                }
            }

            if (session.originalDoseTimes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.7f))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "💡 تجمیع و رند خودکار هوشمند:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ساعات اولیه داروها: " + session.originalDoseTimes.joinToString("، ") { "${it.first} (${it.second})" } + " که برای آسایش مصرف و جلوگیری از آلارم‌های مکرر به ساعت ${session.formattedTime} رند شدند.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PictorialMedicineCard(
    doseItem: MedicineDoseItem,
    batchHour: Int = 8,
    isVisualMode: Boolean = false,
    onMarkTaken: () -> Unit,
    onMarkSkipped: () -> Unit
) {
    val medicine = doseItem.medicine
    val cardBg = getPastelColorByKey(medicine.pastelColorKey)

    PastelCard(
        backgroundColor = cardBg,
        borderColor = SoftOutline,
        cornerRadius = 24.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // تصویر بزرگ و مصور دارو
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isVisualMode) 220.dp else 180.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(medicine.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = medicine.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(cardBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getMedicineTypeIcon(medicine.type),
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )

                // تگ تأیید شده عکس دارو در گوشه تصویر
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                ) {
                    PastelBadge(
                        text = medicine.imageSourceTag.ifBlank { "تأیید شده آنلاین ✓" },
                        backgroundColor = Color.White.copy(alpha = 0.9f),
                        textColor = MaterialTheme.colorScheme.primary
                    )
                }

                // بج نوع دارو در گوشه چپ
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    PastelBadge(
                        text = medicine.type,
                        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                        textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        icon = getMedicineTypeIcon(medicine.type)
                    )
                }
            }

            // در حالت بی‌سواد: ردیف نمادهای بصری فوق‌العاده درشت
            if (isVisualMode) {
                Spacer(modifier = Modifier.height(10.dp))
                IlliterateMedicineVisualRow(
                    dosageText = medicine.dosage,
                    medicineType = medicine.type,
                    instructions = medicine.instructions,
                    hour = batchHour,
                    isExtraLarge = true
                )
            }

            // نمایش انیمیشن تعاملی و متحرک برای مصارف خاص (اسپری، اپلیکاتور، شربت، قطره، جوشان)
            val isSpecialMed = medicine.type.contains("اسپری") || medicine.type.contains("اپلیکاتور") ||
                    medicine.type.contains("شربت") || medicine.type.contains("قطره") ||
                    medicine.type.contains("جوشان") || medicine.dosage.contains("پاف") ||
                    medicine.dosage.contains("اپلیکاتور")

            if (isSpecialMed) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.9f))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    com.example.ui.components.SpecialIntakeAnimatedCanvas(
                        medicineType = medicine.type,
                        dosageText = medicine.dosage,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "نحوه مصرف تصویری متحرک:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when {
                                medicine.type.contains("اپلیکاتور") || medicine.dosage.contains("اپلیکاتور") ->
                                    "کپسول/پماد دارای اپلیکاتور؛ با پیستون به آرامی تخلیه شود."
                                medicine.type.contains("اسپری") || medicine.dosage.contains("پاف") ->
                                    "اسپری را تکان داده، در دهان قرار داده و هم‌زمان با دم عمیق فشار دهید."
                                medicine.type.contains("شربت") || medicine.dosage.contains("قاشق") ->
                                    "شربت را با قاشق اندازه‌گیری دقیق تا خط نشان پر کنید."
                                medicine.type.contains("قطره") ->
                                    "تعداد قطره مشخص شده را با احتیاط بچکانید."
                                medicine.type.contains("جوشان") ->
                                    "قرص را در یک لیوان آب کامل حل کرده و میل نمایید."
                                else -> "راهنمای تصویری برای استفاده دقیق و بدون اشتباه دارو"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // نام و دوز مصرفی دارو همراه با نماد گرافیکی دوز
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // نماد بصری قرص، نصف قرص یا پاف
                    VisualDosageIcon(
                        dosageText = medicine.dosage,
                        medicineType = medicine.type,
                        size = 48.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = medicine.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "دوز مصرفی: ${medicine.dosage}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // نشانگر وضعیت مصرف
                when (doseItem.status) {
                    "TAKEN" -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SuccessGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "مصرف شد",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    "SKIPPED" -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(ErrorRed.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "رد شد",
                                style = MaterialTheme.typography.labelSmall,
                                color = ErrorRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(WarningOrange.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "در انتظار مصرف",
                                style = MaterialTheme.typography.labelSmall,
                                color = WarningOrange,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // دستور مصرف و نکات
            if (medicine.instructions.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.8f))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "دستور مصرف: ${medicine.instructions}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            if (medicine.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "یادداشت: ${medicine.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // دکمه‌های عملیاتی دارو
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PastelActionButton(
                    text = if (doseItem.status == "TAKEN") "مصرف شد ✓" else "ثبت مصرف",
                    onClick = onMarkTaken,
                    isTaken = doseItem.status == "TAKEN",
                    modifier = Modifier.weight(1f),
                    testTag = "action_take_${doseItem.medicine.id}"
                )

                if (doseItem.status != "TAKEN") {
                    PastelSkipButton(
                        onClick = onMarkSkipped,
                        modifier = Modifier.weight(0.6f),
                        testTag = "action_skip_${doseItem.medicine.id}"
                    )
                }
            }
        }
    }
}
