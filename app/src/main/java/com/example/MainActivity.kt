package com.example

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.alarm.AlarmSoundManager
import com.example.ui.MainViewModel
import com.example.ui.MedicineFormViewModel
import com.example.ui.components.NavTab
import com.example.ui.components.PastelBottomBar
import com.example.ui.screens.AddEditMedicineScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MedicineListScreen
import com.example.ui.screens.SessionDetailScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MedRemindTheme

enum class ScreenState {
    MAIN_TABS,
    SESSION_DETAIL,
    ADD_EDIT_MEDICINE
}

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels {
        val app = application as MedRemindApplication
        MainViewModel.Factory(app.medicineRepository, app.preferencesRepository, app.alarmScheduler)
    }

    private val medicineFormViewModel: MedicineFormViewModel by viewModels {
        val app = application as MedRemindApplication
        MedicineFormViewModel.Factory(app.medicineRepository, app.alarmScheduler)
    }

    // State اختصاصی برای باز شدن مستقیم از نوتیفیکیشن
    private val pendingNotificationSession = mutableStateOf<Pair<Int, Int>?>(null)

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        handleIntent(intent)

        setContent {
            val userSettings by mainViewModel.userSettings.collectAsState()
            val notificationTarget by pendingNotificationSession

            MedRemindTheme(
                paletteKey = userSettings.paletteKey,
                fontScale = userSettings.fontScale,
                fontWeightLevel = userSettings.fontWeightLevel
            ) {
                // جهت راست‌به‌چپ برای زبان فارسی
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MedRemindAppRoot(
                        mainViewModel = mainViewModel,
                        medicineFormViewModel = medicineFormViewModel,
                        notificationTargetSession = notificationTarget,
                        onClearNotificationTarget = {
                            pendingNotificationSession.value = null
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // متوقف کردن صدای آلارم هنگام ورود به برنامه
        AlarmSoundManager.stopTone()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val targetScreen = intent.getStringExtra(EXTRA_TARGET_SCREEN)
        if (targetScreen == SCREEN_SESSION_DETAIL) {
            val hour = intent.getIntExtra(EXTRA_SESSION_HOUR, 8)
            val minute = intent.getIntExtra(EXTRA_SESSION_MINUTE, 0)
            pendingNotificationSession.value = Pair(hour, minute)
            AlarmSoundManager.stopTone()
        }
    }

    companion object {
        const val EXTRA_TARGET_SCREEN = "extra_target_screen"
        const val EXTRA_SESSION_HOUR = "extra_session_hour"
        const val EXTRA_SESSION_MINUTE = "extra_session_minute"
        const val SCREEN_SESSION_DETAIL = "screen_session_detail"
    }
}

@Composable
fun MedRemindAppRoot(
    mainViewModel: MainViewModel,
    medicineFormViewModel: MedicineFormViewModel,
    notificationTargetSession: Pair<Int, Int>?,
    onClearNotificationTarget: () -> Unit
) {
    var currentScreen by remember { mutableStateOf(ScreenState.MAIN_TABS) }
    var currentTab by remember { mutableStateOf(NavTab.TODAY) }
    var selectedHour by remember { mutableIntStateOf(8) }
    var selectedMinute by remember { mutableIntStateOf(0) }
    var editingMedicineId by remember { mutableLongStateOf(0L) }

    // هدایت ۱۰۰٪ تضمینی هنگام لمس نوتیفیکیشن
    LaunchedEffect(notificationTargetSession) {
        notificationTargetSession?.let { (h, m) ->
            selectedHour = h
            selectedMinute = m
            currentScreen = ScreenState.SESSION_DETAIL
            onClearNotificationTarget()
        }
    }

    Scaffold(
        bottomBar = {
            if (currentScreen == ScreenState.MAIN_TABS) {
                PastelBottomBar(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it }
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentScreen, label = "screenTransition") { screen ->
                when (screen) {
                    ScreenState.MAIN_TABS -> {
                        when (currentTab) {
                            NavTab.TODAY -> HomeScreen(
                                viewModel = mainViewModel,
                                onNavigateToSession = { hour, minute ->
                                    selectedHour = hour
                                    selectedMinute = minute
                                    currentScreen = ScreenState.SESSION_DETAIL
                                },
                                onNavigateToAddMedicine = {
                                    editingMedicineId = 0L
                                    medicineFormViewModel.resetForm()
                                    currentScreen = ScreenState.ADD_EDIT_MEDICINE
                                }
                            )
                            NavTab.MEDICINES -> MedicineListScreen(
                                viewModel = mainViewModel,
                                onNavigateToAddMedicine = {
                                    editingMedicineId = 0L
                                    medicineFormViewModel.resetForm()
                                    currentScreen = ScreenState.ADD_EDIT_MEDICINE
                                },
                                onNavigateToEditMedicine = { medId ->
                                    editingMedicineId = medId
                                    medicineFormViewModel.loadMedicine(medId)
                                    currentScreen = ScreenState.ADD_EDIT_MEDICINE
                                }
                            )
                            NavTab.HISTORY -> HistoryScreen(
                                viewModel = mainViewModel
                            )
                            NavTab.SETTINGS -> SettingsScreen(
                                viewModel = mainViewModel
                            )
                        }
                    }

                    ScreenState.SESSION_DETAIL -> {
                        SessionDetailScreen(
                            batchHour = selectedHour,
                            batchMinute = selectedMinute,
                            viewModel = mainViewModel,
                            onBack = {
                                currentScreen = ScreenState.MAIN_TABS
                            }
                        )
                    }

                    ScreenState.ADD_EDIT_MEDICINE -> {
                        AddEditMedicineScreen(
                            medicineId = editingMedicineId,
                            viewModel = medicineFormViewModel,
                            onNavigateBack = {
                                mainViewModel.loadTodaySessions()
                                currentScreen = ScreenState.MAIN_TABS
                            }
                        )
                    }
                }
            }
        }
    }
}
