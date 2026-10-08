package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.local.MedicineEntity
import com.example.ui.MainViewModel
import com.example.ui.components.PastelBadge
import com.example.ui.components.PastelCard
import com.example.ui.components.VisualDosageIcon
import com.example.ui.components.VisualIntakeRuleBadge
import com.example.ui.components.getMedicineTypeIcon
import com.example.ui.components.getPastelColorByKey
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SoftOutline
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun MedicineListScreen(
    viewModel: MainViewModel,
    onNavigateToAddMedicine: () -> Unit,
    onNavigateToEditMedicine: (Long) -> Unit
) {
    val medicines by viewModel.allMedicines.collectAsState()
    var medicineToDelete by remember { mutableStateOf<MedicineEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddMedicine,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_medicine")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "افزودن دارو")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("medicine_list_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "داروهای من",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "مدیریت داروها، عکس‌ها و یادآورها (${medicines.size} دارو ثبت شده)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            if (medicines.isEmpty()) {
                item {
                    EmptyStateCard(onAddMedicine = onNavigateToAddMedicine)
                }
            } else {
                items(medicines, key = { it.id }) { med ->
                    MedicineManagementItem(
                        medicine = med,
                        onToggleActive = { viewModel.toggleMedicineActive(med) },
                        onEdit = { onNavigateToEditMedicine(med.id) },
                        onDelete = { medicineToDelete = med }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // دیالوگ تأیید حذف دارو
    medicineToDelete?.let { med ->
        AlertDialog(
            onDismissRequest = { medicineToDelete = null },
            title = { Text(text = "حذف دارو") },
            text = { Text("آیا از حذف داروی «${med.name}» و آلارم‌های مربوط به آن اطمینان دارید؟") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteMedicine(med)
                        medicineToDelete = null
                    }
                ) {
                    Text("حذف", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { medicineToDelete = null }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
fun MedicineManagementItem(
    medicine: MedicineEntity,
    onToggleActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val cardBg = getPastelColorByKey(medicine.pastelColorKey)

    PastelCard(
        backgroundColor = cardBg,
        borderColor = SoftOutline,
        cornerRadius = 22.dp,
        modifier = Modifier.testTag("med_item_${medicine.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // عکس دارو
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(medicine.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = medicine.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = medicine.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${medicine.type} • دوز: ${medicine.dosage}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (medicine.isAsNeeded) {
                        Text(
                            text = "مواقع نیاز (PRN - بدون ساعت ثابت) 🩺",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE11D48),
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "هر ${medicine.frequencyHours} ساعت (شروع: ${String.format(Locale.getDefault(), "%02d:%02d", medicine.firstIntakeHour, medicine.firstIntakeMinute)})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // نماد تصویری دوز مصرفی (قرص، نصف قرص، پاف و...)
                VisualDosageIcon(
                    dosageText = medicine.dosage,
                    medicineType = medicine.type,
                    size = 42.dp
                )

                Spacer(modifier = Modifier.width(8.dp))

                // سوئیچ فعال/غیرفعال آلارم
                Switch(
                    checked = medicine.isActive,
                    onCheckedChange = { onToggleActive() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("switch_active_${medicine.id}")
                )
            }

            if (medicine.instructions.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.7f))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "دستور مصرف: ${medicine.instructions}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    VisualIntakeRuleBadge(instructions = medicine.instructions)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ردیف دکمه‌های اکشن
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PastelBadge(
                    text = medicine.imageSourceTag.ifBlank { "عکس نمونه" },
                    backgroundColor = Color.White.copy(alpha = 0.85f),
                    textColor = MaterialTheme.colorScheme.primary
                )

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.testTag("edit_med_${medicine.id}")) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "ویرایش",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_med_${medicine.id}")) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "حذف",
                            tint = ErrorRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
