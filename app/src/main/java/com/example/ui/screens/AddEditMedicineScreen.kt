package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.ui.text.input.KeyboardType
import android.Manifest
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.ui.MedicineFormViewModel
import com.example.ui.components.PastelBadge
import com.example.ui.components.PastelCard
import com.example.ui.components.getMedicineTypeIcon
import com.example.ui.components.getPastelColorByKey
import com.example.ui.theme.CardLemon
import com.example.ui.theme.CardLavender
import com.example.ui.theme.CardMint
import com.example.ui.theme.CardPeach
import com.example.ui.theme.CardRose
import com.example.ui.theme.CardSky
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SoftOutline
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.ImageStorageUtil
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMedicineScreen(
    medicineId: Long = 0L,
    viewModel: MedicineFormViewModel,
    onNavigateBack: () -> Unit
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current

    LaunchedEffect(medicineId) {
        if (medicineId > 0) {
            viewModel.loadMedicine(medicineId)
        } else {
            viewModel.resetForm()
        }
    }

    val state by viewModel.uiState.collectAsState()
    var showImageSearchDialog by remember { mutableStateOf(false) }
    var imageSearchQuery by remember { mutableStateOf("") }
    var tempCameraFile by remember { mutableStateOf<File?>(null) }
    var showTimePickerPopup by remember { mutableStateOf(false) }

    // ۱. لانچر انتخاب عکس از گالری (Android Photo Picker)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = ImageStorageUtil.saveGalleryUriToInternalStorage(context, uri)
            if (savedPath != null) {
                viewModel.setCustomImage(savedPath, "انتخاب از گالری ✓")
            }
        }
    }

    // ۲. لانچر عکس گرفتن با دوربین با ذخیره امن
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempCameraFile != null) {
            val savedPath = ImageStorageUtil.moveTempCameraFileToInternal(context, tempCameraFile!!)
            if (savedPath != null) {
                viewModel.setCustomImage(savedPath, "عکس از دوربین ✓")
            }
        }
    }

    // ۳. لانچر درخواست دسترسی دوربین
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val (uri, file) = ImageStorageUtil.createTempCameraUri(context)
            tempCameraFile = file
            cameraLauncher.launch(uri)
        }
    }

    val medicineTypes = listOf(
        "قرص", "کپسول", "کپسول با اپلیکاتور", "شربت", "اسپری",
        "قطره", "آمپول", "پماد", "قرص جوشان", "زیر زبانی"
    )
    val commonDosages = listOf(
        "نصف قرص (۱/۲)", "یک چهارم قرص (۱/۴)", "۱ عدد قرص", "۲ عدد قرص",
        "۱ پاف اسپری", "۲ پاف اسپری",
        "۵ میلی‌لیتر (۱ قاشق)", "۱۰ میلی‌لیتر (۲ قاشق)",
        "کپسول با اپلیکاتور", "۲ قطره", "مواقع نیاز (درد/تب)"
    )
    val frequencyOptions = listOf(
        Pair("هر ۶ ساعت (۴ بار در روز)", 6),
        Pair("هر ۸ ساعت (۳ بار در روز)", 8),
        Pair("هر ۱۲ ساعت (۲ بار در روز)", 12),
        Pair("هر ۲۴ ساعت (۱ بار در روز)", 24)
    )
    val colorKeys = listOf(
        Pair("نعنایی", "mint"),
        Pair("یاسی", "lavender"),
        Pair("هلویی", "peach"),
        Pair("رز", "rose"),
        Pair("آسمانی", "sky"),
        Pair("لیمویی", "lemon")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.id == 0L) "افزودن داروی جدید" else "ویرایش اطلاعات دارو",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("add_med_back_button")
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
                            viewModel.saveMedicine {
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("save_medicine_button"),
                        shape = RoundedCornerShape(18.dp),
                        enabled = !state.isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ذخیره دارو و تنظیم هوشمند آلارم",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // پیام خطا در صورت وجود
            if (state.errorMessage != null) {
                item {
                    PastelCard(
                        backgroundColor = ErrorRed.copy(alpha = 0.1f),
                        borderColor = ErrorRed.copy(alpha = 0.4f),
                        cornerRadius = 16.dp
                    ) {
                        Text(
                            text = state.errorMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ErrorRed
                        )
                    }
                }
            }

            // بخش تصویر مصور دارو و جستجوی آنلاین
            item {
                PastelCard(
                    backgroundColor = getPastelColorByKey(state.pastelColorKey),
                    borderColor = SoftOutline,
                    cornerRadius = 24.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "تصویر دارو (جستجوی آنلاین)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // پیش‌نمایش تصویر دارو
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                        ) {
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(state.imageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = state.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                                loading = {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(32.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            )

                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                            ) {
                                PastelBadge(
                                    text = state.imageSourceTag,
                                    backgroundColor = Color.White.copy(alpha = 0.9f),
                                    textColor = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "روش افزودن تصویر دارو:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // ردیف دکمه‌های دوربین و گالری
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("btn_camera_photo"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "دوربین",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    galleryLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("btn_gallery_photo"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "گالری",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // دکمه باز کردن پنل جستجوی عکس آنلاین
                        OutlinedButton(
                            onClick = {
                                showImageSearchDialog = !showImageSearchDialog
                                if (imageSearchQuery.isBlank() && state.name.isNotBlank()) {
                                    imageSearchQuery = state.name
                                    viewModel.searchMedicineImages(state.name)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("search_medicine_image_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (showImageSearchDialog) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (showImageSearchDialog) "بستن نتایج جستجوی اینترنتی" else "🔍 جستجوی خودکار عکس دارو از اینترنت",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // پنل بازشونده جستجوی اینترنتی عکس دارو
                        AnimatedVisibility(visible = showImageSearchDialog) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.9f))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = imageSearchQuery,
                                        onValueChange = { imageSearchQuery = it },
                                        placeholder = { Text("نام دارو را جستجو کنید (مثلاً امپرازول)...", style = MaterialTheme.typography.bodySmall) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("image_search_query_input"),
                                        shape = RoundedCornerShape(14.dp),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { viewModel.searchMedicineImages(imageSearchQuery) },
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.testTag("execute_image_search_button")
                                    ) {
                                        Text("جستجو")
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                if (state.isSearchingImages) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(28.dp))
                                    }
                                } else {
                                    Text(
                                        text = "یک عکس را انتخاب و تأیید کنید:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(state.searchResults) { item ->
                                            val isSelected = state.imageUrl == item.imageUrl
                                            Box(
                                                modifier = Modifier
                                                    .size(100.dp)
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .border(
                                                        width = if (isSelected) 3.dp else 1.dp,
                                                        color = if (isSelected) MaterialTheme.colorScheme.primary else SoftOutline,
                                                        shape = RoundedCornerShape(14.dp)
                                                    )
                                                    .clickable {
                                                        viewModel.selectImage(item)
                                                    }
                                            ) {
                                                SubcomposeAsyncImage(
                                                    model = item.imageUrl,
                                                    contentDescription = item.title,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                                if (isSelected) {
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.BottomEnd)
                                                            .padding(4.dp)
                                                            .size(22.dp)
                                                            .clip(CircleShape)
                                                            .background(MaterialTheme.colorScheme.primary),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // فیلد نام دارو
            item {
                PastelCard(cornerRadius = 20.dp) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "نام دارو *",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = state.name,
                            onValueChange = {
                                viewModel.updateName(it)
                            },
                            placeholder = { Text("مثلاً استامینوفن یا Amoxicillin") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("medicine_name_input"),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = SoftOutline
                            ),
                            singleLine = true
                        )
                    }
                }
            }

            // انتخاب نوع دارو با چیپ‌های پاستیلی
            item {
                PastelCard(cornerRadius = 20.dp) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "نوع و شکل دارو",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(medicineTypes) { type ->
                                val isSelected = state.type == type
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.updateType(type) },
                                    label = { Text(type) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = getMedicineTypeIcon(type),
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // دوز مصرفی دارو با نماد گرافیکی و انیمیشن زنده
            item {
                PastelCard(cornerRadius = 20.dp) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "دوز مصرفی در هر نوبت",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "نماد بصری برای افراد باسواد و بی‌سواد:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                            // نماد بصری قرص، نصف قرص، قاشق، پاف، اپلیکاتور
                            com.example.ui.components.VisualDosageIcon(
                                dosageText = state.dosage,
                                medicineType = state.type,
                                size = 52.dp
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        // مقدار مصرف در هر نوبت + واحد (مثلاً ۲ قاشق، ۱ پاف)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = state.doseAmount,
                                onValueChange = { viewModel.updateDoseAmount(it) },
                                label = { Text("مقدار") },
                                modifier = Modifier
                                    .width(104.dp)
                                    .testTag("medicine_dose_amount"),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DoseUnits.forEach { unit ->
                                    FilterChip(
                                        selected = state.doseUnit == unit,
                                        onClick = { viewModel.updateDoseUnit(unit) },
                                        label = { Text(unit) }
                                    )
                                }
                            }
                        }
                        OutlinedTextField(
                            value = state.dosage,
                            onValueChange = { viewModel.updateDosage(it) },
                            placeholder = { Text("مثلاً ۱ عدد قرص، نصف قرص، ۲ پاف، ۱۰ میلی‌لیتر...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("medicine_dosage_input"),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "انتخاب سریع دوز و تعداد (همراه با نماد بصری):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(commonDosages) { dose ->
                                val isSelected = state.dosage == dose
                                PastelBadge(
                                    text = dose,
                                    backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    textColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else TextPrimary,
                                    modifier = Modifier.clickable { viewModel.updateDosage(dose) }
                                )
                            }
                        }

                        // پیش‌نمایش انیمیشنی Canvas برای مصارف خاص (اسپری، اپلیکاتور، شربت، قطره، جوشان)
                        val isSpecialType = state.type.contains("اسپری") || state.type.contains("اپلیکاتور") ||
                                state.type.contains("شربت") || state.type.contains("قطره") ||
                                state.type.contains("جوشان") || state.dosage.contains("پاف") ||
                                state.dosage.contains("اپلیکاتور")

                        if (isSpecialType) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    com.example.ui.components.SpecialIntakeAnimatedCanvas(
                                        medicineType = state.type,
                                        dosageText = state.dosage,
                                        modifier = Modifier.size(90.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "انیمیشن آموزش نحوه مصرف خاص ✨",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = when {
                                                state.type.contains("اپلیکاتور") || state.dosage.contains("اپلیکاتور") ->
                                                    "آموزش تصویری حرکت پیستون و هدایت کپسول در اپلیکاتور برای افراد کم‌سواد"
                                                state.type.contains("اسپری") || state.dosage.contains("پاف") ->
                                                    "نشانگر فشرده‌شدن محفظه و پخش ابرهای پاف استنشاقی"
                                                state.type.contains("شربت") || state.dosage.contains("قاشق") ->
                                                    "آموزش پر کردن قاشق شربت‌خوری و میزان دقیق سی‌سی"
                                                state.type.contains("قطره") ->
                                                    "نمایش چکیدن قطره بهداشتی در محل مصرف"
                                                state.type.contains("جوشان") ->
                                                    "حل شدن قرص جوشان با حباب‌های فعال در آب"
                                                else -> "راهنمای متحرک مصرف صحیح دارو"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // انتخاب مدل مصرف: منظم با آلارم یا در مواقع نیاز (PRN)
            item {
                PastelCard(cornerRadius = 20.dp) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HealthAndSafety,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "مدل زمان‌بندی مصرف دارو",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        // گزینه ۱: مصرف منظم
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (!state.isAsNeeded) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                    else Color.Transparent
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (!state.isAsNeeded) MaterialTheme.colorScheme.primary else SoftOutline,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { viewModel.updateIsAsNeeded(false) }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "مصرف منظم طبق ساعت و آلارم روزانه ⏰",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (!state.isAsNeeded) FontWeight.Bold else FontWeight.Normal,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "دارو در ساعات مشخص پخش آلارم دارد (مثلاً هر ۸ ساعت)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            if (!state.isAsNeeded) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // گزینه ۲: مواقع نیاز (PRN)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (state.isAsNeeded) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                    else Color.Transparent
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (state.isAsNeeded) MaterialTheme.colorScheme.primary else SoftOutline,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { viewModel.updateIsAsNeeded(true) }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "در مواقع نیاز و لزوم (PRN) 🩺",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (state.isAsNeeded) FontWeight.Bold else FontWeight.Normal,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "هنگام بروز درد، تب، سوزش یا تهوع (بدون ساعت اجباری ثابت)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            if (state.isAsNeeded) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // فاصله زمانی تکرار و ساعات مصرف
            item {
                PastelCard(cornerRadius = 20.dp) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "فاصله زمانی و تعداد دفعات مصرف در شبانه‌روز",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        frequencyOptions.forEach { (label, hours) ->
                            val isSelected = state.frequencyHours == hours
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                        else Color.Transparent
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else SoftOutline,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable { viewModel.updateFrequencyHours(hours) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else TextPrimary
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ساعت و دقیقه اولین استفاده (با پاپ‌آپ ساعت عقربه‌ای/دیجیتال)
            item {
                PastelCard(cornerRadius = 20.dp) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ساعت اولین استفاده در روز",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            PastelBadge(
                                text = String.format(Locale.getDefault(), "%02d:%02d", state.firstHour, state.firstMinute),
                                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                                textColor = MaterialTheme.colorScheme.primary,
                                icon = Icons.Default.AccessTime
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "برای انتخاب دقیق ساعت و دقیقه، دکمه زیر را لمس کنید تا ساعت پاپ‌آپ باز شود:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        // دکمه باز کردن پاپ‌آپ ساعت
                        Button(
                            onClick = { showTimePickerPopup = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_open_time_picker"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تنظیم ساعت با ساعت پاپ‌آپ ⏰ (${String.format(Locale.getDefault(), "%02d:%02d", state.firstHour, state.firstMinute)})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // دستورات و توضیحات مصرف با نمادهای تصویری
            item {
                PastelCard(cornerRadius = 20.dp) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "نحوه و دستورات مصرف",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            com.example.ui.components.VisualIntakeRuleBadge(
                                instructions = state.instructions
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.instructions,
                            onValueChange = { viewModel.updateInstructions(it) },
                            placeholder = { Text("مثلاً همراه غذا، قبل خواب، با آب فراوان...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("medicine_instructions_input"),
                            shape = RoundedCornerShape(16.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "انتخاب سریع دستور مصرف با نماد گرافیکی:",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(listOf(
                                "ناشتا (نیم ساعت قبل صبحانه)",
                                "قبل از غذا",
                                "همراه با غذا",
                                "بعد از غذا",
                                "با آب فراوان",
                                "قبل از خواب",
                                "مواقع نیاز (درد/تب)",
                                "زیر زبانی",
                                "موضعی / مالیدنی",
                                "با اپلیکاتور"
                            )) { rule ->
                                com.example.ui.components.VisualIntakeRuleBadge(
                                    instructions = rule,
                                    modifier = Modifier.clickable { viewModel.updateInstructions(rule) }
                                )
                            }
                        }
                    }
                }
            }

            // انتخاب رنگ پاستیلی کارت دارو
            item {
                PastelCard(cornerRadius = 20.dp) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "رنگ پاستیلی کارت برای تمایز دارو",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            colorKeys.forEach { (label, key) ->
                                val isSelected = state.pastelColorKey == key
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable { viewModel.updatePastelColorKey(key) }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(getPastelColorByKey(key))
                                            .border(
                                                width = if (isSelected) 2.5.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else SoftOutline,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // یادداشت‌های تکمیلی
            item {
                PastelCard(cornerRadius = 20.dp) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "یادداشت‌ها و توصیه‌های ویژه پزشک (اختیاری)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = state.notes,
                            onValueChange = { viewModel.updateNotes(it) },
                            placeholder = { Text("مثلاً طول درمان ۲ هفته، با لبنیات مصرف نشود...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("medicine_notes_input"),
                            shape = RoundedCornerShape(16.dp),
                            minLines = 2
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // پاپ‌آپ ساعت برای انتخاب دقیق ساعت و دقیقه اولین نوبت مصرف
    if (showTimePickerPopup) {
        val timePickerState = rememberTimePickerState(
            initialHour = state.firstHour,
            initialMinute = state.firstMinute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePickerPopup = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "انتخاب ساعت اولین مصرف دارو ⏰",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateFirstTime(timePickerState.hour, timePickerState.minute)
                        showTimePickerPopup = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("تأیید ساعت", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePickerPopup = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}


/** واحدهای مقدار مصرف */
private val DoseUnits = listOf("عدد", "قاشق", "میلی‌لیتر", "پاف", "قطره", "اپلیکاتور")
