package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.repository.ClusteredIntakeSession
import com.example.ui.MainViewModel
import com.example.ui.components.IlliterateMedicineVisualRow
import com.example.ui.components.PastelAnalogClock
import com.example.ui.components.PastelBadge
import com.example.ui.components.PastelCard
import com.example.ui.components.VisualDosageIcon
import com.example.ui.components.VisualIntakeRuleBadge
import com.example.ui.components.getPastelColorByKey
import com.example.ui.theme.CardLemon
import com.example.ui.theme.CardMint
import com.example.ui.theme.CardPeach
import com.example.ui.theme.SoftOutline
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningOrange
import com.example.util.PersianDateHelper
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.WbSunny

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToSession: (hour: Int, minute: Int) -> Unit,
    onNavigateToAddMedicine: () -> Unit
) {
    val sessions by viewModel.dailySessions.collectAsState()
    val isLoading by viewModel.isLoadingSessions.collectAsState()
    val allMedicines by viewModel.allMedicines.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()

    val totalDoses = sessions.sumOf { it.doseItems.size }
    val takenDoses = sessions.sumOf { it.doseItems.count { dose -> dose.status == "TAKEN" } }
    val progressPercent = if (totalDoses > 0) (takenDoses * 100) / totalDoses else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { PermissionStatusCard() }

        // بنر هدر گرافیکی پاستیلی
        item {
            PastelHeroBanner(
                progressPercent = progressPercent,
                takenDoses = takenDoses,
                totalDoses = totalDoses,
                onRefresh = { viewModel.loadTodaySessions() }
            )
        }

        // ویجت ساعت آنالوگ پاستیلی و تقویم رسمی شمسی
        item {
            PastelAnalogClockAndDateCard()
        }

        // عنوان بخش نوبت‌های امروز
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (userSettings.isVisualAccessibilityMode) "نوبت‌های امروز (حالت مصور بی‌سواد)" else "نوبت‌های تجمیعی امروز",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (userSettings.isVisualAccessibilityMode) "نمایش کاملاً تصویری همراه با نماد قرص، پاف و غذا" else "آلارم‌های هوشمند رند شده بر حسب زمان مصرف",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                }
            }
        }

        // در صورت نبود هیچ دارویی
        if (allMedicines.isEmpty()) {
            item {
                EmptyStateCard(onAddMedicine = onNavigateToAddMedicine)
            }
        } else if (sessions.isEmpty()) {
            item {
                PastelCard(cornerRadius = 20.dp) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "داروهای شما غیرفعال هستند یا نوبتی برای امروز ثبت نشده است.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            // لیست نوبت‌های تجمیعی
            items(sessions, key = { "${it.batchHour}_${it.batchMinute}" }) { session ->
                SessionCardItem(
                    session = session,
                    isVisualMode = userSettings.isVisualAccessibilityMode,
                    onOpenSession = { onNavigateToSession(session.batchHour, session.batchMinute) },
                    onQuickTakeAll = { viewModel.markAllInSession(session, "TAKEN") }
                )
            }
        }

        // بخش داروهای مواقع نیاز (PRN)
        val prnMedicines = allMedicines.filter { it.isAsNeeded && it.isActive }
        if (prnMedicines.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = null,
                        tint = Color(0xFFE11D48),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "داروهای مواقع نیاز (PRN) 🩺",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Text(
                    text = "داروهایی که بدون ساعت ثابت، در صورت بروز درد، تب یا حساسیت مصرف می‌شوند:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            items(prnMedicines, key = { "prn_${it.id}" }) { prnMed ->
                PrnMedicineCardItem(
                    medicine = prnMed,
                    onTakeNow = {
                        viewModel.recordPrnIntake(prnMed)
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PastelHeroBanner(
    progressPercent: Int,
    takenDoses: Int,
    totalDoses: Int,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // تصویر پس‌زمینه لطیف
            Image(
                painter = painterResource(id = R.drawable.img_pastel_health_hero),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(26.dp)),
                alpha = 0.85f
            )

            // لایه گرادیان و محتوا
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(26.dp)
                    )
                    .padding(18.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "دارویاد • یادآور هوشمند",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Text(
                                text = "سلامتی با مصرف منظم دارو",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "به‌روزرسانی",
                                tint = Color.White
                            )
                        }
                    }

                    // ویجت پیشرفت روزانه
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.9f))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "پیشرفت مصرف امروز",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "$takenDoses از $totalDoses دوز مصرف شده",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Text(
                            text = "$progressPercent٪",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SessionCardItem(
    session: ClusteredIntakeSession,
    isVisualMode: Boolean = false,
    onOpenSession: () -> Unit,
    onQuickTakeAll: () -> Unit
) {
    PastelCard(
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = if (session.isAllTaken) SuccessGreen.copy(alpha = 0.4f) else SoftOutline,
        cornerRadius = 24.dp,
        onClick = onOpenSession,
        modifier = Modifier.testTag("session_card_${session.batchHour}_${session.batchMinute}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DoseStatusOrb(
                        status = if (session.isAllTaken) "TAKEN" else "PENDING",
                        hour = session.batchHour,
                        onTap = if (session.isAllTaken) null else onQuickTakeAll,
                        modifier = Modifier,
                        orbSize = 72.dp,
                        iconSize = 40.dp,
                        showTime = false
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = session.formattedTime,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${session.doseItems.size} دارو در این نوبت",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                if (session.isAllTaken) {
                    PastelBadge(
                        text = "مصرف شد ✓",
                        backgroundColor = SuccessGreen.copy(alpha = 0.15f),
                        textColor = SuccessGreen
                    )
                } else {
                    PastelBadge(
                        text = "در انتظار",
                        backgroundColor = WarningOrange.copy(alpha = 0.15f),
                        textColor = WarningOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // در حالت بی‌سواد / مصور: نمایش ردیف نمادهای بصری و درشت
            if (isVisualMode) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    session.doseItems.forEach { item ->
                        val med = item.medicine
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(getPastelColorByKey(med.pastelColorKey))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(med.imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = med.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = med.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = med.dosage,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                VisualDosageIcon(
                                    dosageText = med.dosage,
                                    medicineType = med.type,
                                    size = 44.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                VisualIntakeRuleBadge(
                                    instructions = med.instructions
                                )
                            }
                        }
                    }
                }
            } else {
                // حالت استاندارد با نمادهای گرافیکی متوسط
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(session.doseItems) { item ->
                        val med = item.medicine
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(getPastelColorByKey(med.pastelColorKey))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(med.imageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = med.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = med.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = med.dosage,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            VisualDosageIcon(
                                dosageText = med.dosage,
                                medicineType = med.type,
                                size = 32.dp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // دکمه‌های عملیاتی کارت
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.clickable { onOpenSession() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مشاهده لیست مصور این ساعت",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                if (session.isPending) {
                    Button(
                        onClick = onQuickTakeAll,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "ثبت مصرف همه", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

/**
 * کارت ساعت آنالوگ عقربه‌ای روان و تقویم رسمی خورشیدی
 */
@Composable
fun PastelAnalogClockAndDateCard() {
    val persianDate = remember { PersianDateHelper.getTodayPersianDate() }

    PastelCard(
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = SoftOutline,
        cornerRadius = 24.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // سمت راست (RTL): تقویم خورشیدی و پیام سلامت
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = persianDate.dayOfWeekName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${persianDate.day} ${persianDate.monthName} ${persianDate.year}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                PastelBadge(
                    text = "تقویم رسمی خورشیدی",
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    textColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // سمت چپ (RTL): ساعت آنالوگ عقربه‌ای روان پاستیلی
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                PastelAnalogClock(
                    size = 94.dp,
                    dialColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                    primaryColor = MaterialTheme.colorScheme.primary,
                    accentColor = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ساعت زنده ⏰",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard(onAddMedicine: () -> Unit) {
    PastelCard(
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = SoftOutline,
        cornerRadius = 24.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_pastel_success_box),
                contentDescription = null,
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "هنوز دارویی ثبت نکرده‌اید!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "برای شروع، داروی مورد نظر خود را اضافه کنید. اپلیکیشن به صورت خودکار عکس دارو را از اینترنت پیدا می‌کند و آلارم‌های نوبت‌ها را رند و تنظیم می‌نماید.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onAddMedicine,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("empty_state_add_medicine_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "افزودن اولین دارو", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

/**
 * کارت اختصاصی برای داروهای در مواقع نیاز (PRN) با ثبت مصرف فوری و نمادهای گرافیکی
 */
@Composable
fun PrnMedicineCardItem(
    medicine: com.example.data.local.MedicineEntity,
    onTakeNow: () -> Unit
) {
    val cardBg = com.example.ui.components.getPastelColorByKey(medicine.pastelColorKey)
    var isTakenRecently by remember { mutableStateOf(false) }

    PastelCard(
        backgroundColor = cardBg,
        borderColor = SoftOutline,
        cornerRadius = 20.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // تصویر دارو
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(medicine.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = medicine.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = medicine.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        PastelBadge(
                            text = "PRN",
                            backgroundColor = Color(0xFFFFE4E6),
                            textColor = Color(0xFFE11D48)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${medicine.type} • دوز: ${medicine.dosage}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                // نماد گرافیکی دوز
                com.example.ui.components.VisualDosageIcon(
                    dosageText = medicine.dosage,
                    medicineType = medicine.type,
                    size = 40.dp
                )
            }

            if (medicine.instructions.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = medicine.instructions,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    com.example.ui.components.VisualIntakeRuleBadge(instructions = medicine.instructions)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    onTakeNow()
                    isTakenRecently = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTakenRecently) com.example.ui.theme.SuccessGreen else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (isTakenRecently) Icons.Default.CheckCircle else Icons.Default.HealthAndSafety,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isTakenRecently) "مصرف در این لحظه ثبت شد ✓" else "ثبت مصرف در این لحظه (هنگام نیاز)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
