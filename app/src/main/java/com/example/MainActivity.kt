package com.example

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.compatibility.DeviceCompatibilityChecker
import com.example.compatibility.DeviceCompatibilityReport
import com.example.compatibility.FeatureDiagnostic
import com.example.compatibility.PreloadCheckStep
import com.example.ui.EmergencyDashboardScreen
import com.example.ui.EmergencyDashboardViewModel
import com.example.ui.theme.ActionBlue
import com.example.ui.theme.ActionBlueLight
import com.example.ui.theme.CalmGreen
import com.example.ui.theme.CalmGreenLight
import com.example.ui.theme.DayBackground
import com.example.ui.theme.DayBorder
import com.example.ui.theme.DaySurface
import com.example.ui.theme.DaySurfaceVariant
import com.example.ui.theme.DayTextPrimary
import com.example.ui.theme.DayTextSecondary
import com.example.ui.theme.DayTextTertiary
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedLight
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SafetyAmber
import com.example.ui.theme.SafetyAmberLight
import com.example.ui.theme.White

enum class AppStartupFlow {
    FIRST_LAUNCH_PRELOADER,
    DEVICE_UNSUPPORTED,
    PERMISSION_REQUEST,
    DASHBOARD
}

class MainActivity : ComponentActivity() {

    private val viewModel: EmergencyDashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val app = context.applicationContext as SilentBeaconApp

                val prefs = remember {
                    context.getSharedPreferences("silent_beacon_setup_prefs", Context.MODE_PRIVATE)
                }

                // Check if this is the very first install & launch of the application
                val hasCompletedInitialCheck = remember {
                    prefs.getBoolean("has_completed_initial_compatibility_check", false)
                }

                var activeReport by remember {
                    mutableStateOf<DeviceCompatibilityReport?>(
                        if (hasCompletedInitialCheck) DeviceCompatibilityChecker.checkCompatibility(context) else null
                    )
                }

                var currentFlow by remember {
                    mutableStateOf(
                        if (!hasCompletedInitialCheck) {
                            AppStartupFlow.FIRST_LAUNCH_PRELOADER
                        } else {
                            val report = activeReport ?: DeviceCompatibilityChecker.checkCompatibility(context)
                            if (!report.canRunAppSafely) {
                                AppStartupFlow.DEVICE_UNSUPPORTED
                            } else {
                                app.initMeshLayerSafely()
                                AppStartupFlow.PERMISSION_REQUEST
                            }
                        }
                    )
                }

                val requiredPermissions = remember {
                    buildList {
                        add(Manifest.permission.ACCESS_FINE_LOCATION)
                        add(Manifest.permission.ACCESS_COARSE_LOCATION)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            add(Manifest.permission.BLUETOOTH_SCAN)
                            add(Manifest.permission.BLUETOOTH_ADVERTISE)
                            add(Manifest.permission.BLUETOOTH_CONNECT)
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            add(Manifest.permission.NEARBY_WIFI_DEVICES)
                            add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                var allPermissionsGranted by remember {
                    mutableStateOf(
                        requiredPermissions.all { perm ->
                            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
                        }
                    )
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { result ->
                    val granted = requiredPermissions.all { perm ->
                        result[perm] == true || ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
                    }
                    allPermissionsGranted = granted
                    if (granted) {
                        viewModel.startMeshService()
                        viewModel.fetchCurrentLocation()
                        currentFlow = AppStartupFlow.DASHBOARD
                    }
                }

                // Auto-advance to dashboard if permissions are already satisfied and device is compatible
                LaunchedEffect(currentFlow, allPermissionsGranted) {
                    if (currentFlow == AppStartupFlow.PERMISSION_REQUEST) {
                        if (allPermissionsGranted) {
                            viewModel.startMeshService()
                            currentFlow = AppStartupFlow.DASHBOARD
                        }
                    }
                }

                when (currentFlow) {
                    AppStartupFlow.FIRST_LAUNCH_PRELOADER -> {
                        FirstLaunchPreloaderScreen(
                            onPreloadCompleted = { report ->
                                activeReport = report
                                if (report.canRunAppSafely) {
                                    // Mark first-time configuration check completed successfully
                                    prefs.edit().putBoolean("has_completed_initial_compatibility_check", true).apply()
                                    // Safely initialize the Nearby Connections mesh layer
                                    app.initMeshLayerSafely()

                                    if (allPermissionsGranted) {
                                        viewModel.startMeshService()
                                        currentFlow = AppStartupFlow.DASHBOARD
                                    } else {
                                        currentFlow = AppStartupFlow.PERMISSION_REQUEST
                                    }
                                } else {
                                    // Show unsupported screen with full diagnostics
                                    currentFlow = AppStartupFlow.DEVICE_UNSUPPORTED
                                }
                            }
                        )
                    }

                    AppStartupFlow.DEVICE_UNSUPPORTED -> {
                        val report = activeReport ?: DeviceCompatibilityChecker.checkCompatibility(context)
                        DeviceUnsupportedScreen(
                            report = report,
                            onRecheck = {
                                // Re-run verification through the preloader engine
                                currentFlow = AppStartupFlow.FIRST_LAUNCH_PRELOADER
                            }
                        )
                    }

                    AppStartupFlow.PERMISSION_REQUEST -> {
                        val report = activeReport ?: DeviceCompatibilityChecker.checkCompatibility(context)
                        PermissionRequestScreen(
                            report = report,
                            onRequestPermissions = {
                                permissionLauncher.launch(requiredPermissions.toTypedArray())
                            },
                            onProceedAnyway = {
                                allPermissionsGranted = true
                                viewModel.startMeshService()
                                currentFlow = AppStartupFlow.DASHBOARD
                            }
                        )
                    }

                    AppStartupFlow.DASHBOARD -> {
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                        EmergencyDashboardScreen(
                            state = uiState,
                            onDispatchTicket = { cat, urg, title, desc, locDesc, lat, lng ->
                                viewModel.dispatchEmergencyTicket(cat, urg, title, desc, locDesc, lat, lng)
                            },
                            onResolveTicket = { ticketId ->
                                viewModel.markTicketResolved(ticketId)
                            },
                            onAcknowledgeTicket = { ticketId ->
                                viewModel.markTicketAcknowledged(ticketId)
                            },
                            onToggleMesh = { active ->
                                if (active) viewModel.startMeshService() else viewModel.stopMeshService()
                            },
                            onPingHandshake = { endpointId ->
                                viewModel.pingPeerHandshake(endpointId)
                            },
                            onFetchLocation = {
                                viewModel.fetchCurrentLocation()
                            },
                            onSetCategoryFilter = { viewModel.setCategoryFilter(it) },
                            onSetUrgencyFilter = { viewModel.setUrgencyFilter(it) },
                            onSetStatusFilter = { viewModel.setStatusFilter(it) },
                            onSetSearchQuery = { viewModel.setSearchQuery(it) },
                            onSilenceAlarm = { viewModel.silenceAlarm() },
                            onTestAlarm = { viewModel.testEmergencyAlarm() },
                            onDeleteTicket = { ticketId -> viewModel.deleteTicket(ticketId) },
                            onClearAllResolved = { viewModel.clearAllResolvedTickets() },
                            onSimulatePeer = { viewModel.simulateNearbyPeer() },
                            onDisconnectPeer = { peerId -> viewModel.disconnectSimulatedPeer(peerId) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Preloader displayed ONLY when the app is installed and launched for the first time.
 * Runs comprehensive hardware and software checks in the background with safe error handling,
 * displaying real-time progress for each required subsystem.
 */
@Composable
fun FirstLaunchPreloaderScreen(
    onPreloadCompleted: (DeviceCompatibilityReport) -> Unit
) {
    val context = LocalContext.current

    var currentStep by remember { mutableStateOf(PreloadCheckStep.SYSTEM_OS) }
    var currentProgress by remember { mutableFloatStateOf(0.1f) }
    var completedSteps by remember { mutableStateOf<Set<PreloadCheckStep>>(emptySet()) }

    val animatedProgress by animateFloatAsState(
        targetValue = currentProgress,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "preloader_progress"
    )

    LaunchedEffect(Unit) {
        try {
            val report = DeviceCompatibilityChecker.runStepByStepPreloadCheck(context) { step, progress ->
                currentStep = step
                currentProgress = progress
                completedSteps = completedSteps + step
            }
            // Small transition breathing room
            kotlinx.coroutines.delay(200L)
            onPreloadCompleted(report)
        } catch (t: Throwable) {
            // Unhandled exception failsafe: create emergency diagnostic report without crashing
            android.util.Log.e("Preloader", "Error during pre-flight checks", t)
            val fallbackReport = DeviceCompatibilityChecker.checkCompatibility(context)
            onPreloadCompleted(fallbackReport)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DayBackground
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Animated App Icon Badge
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(ActionBlueLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Podcasts,
                        contentDescription = "SilentBeacon Initializing",
                        tint = ActionBlue,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Text(
                    text = "Setting Up SilentBeacon",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = DayTextPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Testing device radios, Google Play Services, and emergency offline storage for disaster mesh readiness...",
                    fontSize = 13.sp,
                    color = DayTextSecondary,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Progress Bar Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = DaySurface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, DayBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.5.dp,
                                    color = ActionBlue
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Step ${currentStep.stepNumber} of 6",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ActionBlue
                                )
                            }
                            Text(
                                text = "${(animatedProgress * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DayTextSecondary
                            )
                        }

                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .testTag("preloader_progress_bar"),
                            color = ActionBlue,
                            trackColor = DaySurfaceVariant,
                        )

                        // Current checking detail
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DaySurfaceVariant, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = currentStep.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = DayTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentStep.description,
                                fontSize = 12.sp,
                                color = DayTextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Real-time Subsystem Verification Checklist
                Card(
                    colors = CardDefaults.cardColors(containerColor = DaySurface),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, DayBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Hardware & Software Checklist",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = DayTextPrimary
                        )

                        PreloadCheckStep.entries.forEach { step ->
                            val isCompleted = completedSteps.contains(step)
                            val isCurrent = currentStep == step && !isCompleted

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isCompleted) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified",
                                        tint = CalmGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else if (isCurrent) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = ActionBlue
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.RadioButtonUnchecked,
                                        contentDescription = "Pending",
                                        tint = DayTextTertiary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    text = step.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isCompleted) DayTextPrimary else if (isCurrent) ActionBlue else DayTextSecondary
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "This pre-flight check runs once during initial install to safely confirm your phone's radios before opening the mesh.",
                    fontSize = 11.sp,
                    color = DayTextTertiary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Screen displayed when device configuration does not support peer-to-peer radio mesh.
 * Outlines missing hardware/software and provides offline disaster survival instructions.
 */
@Composable
fun DeviceUnsupportedScreen(
    report: DeviceCompatibilityReport,
    onRecheck: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DayBackground
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header badge
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(SafetyAmberLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ReportProblem,
                        contentDescription = "Device Not Supported",
                        tint = SafetyAmber,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Text(
                    text = "Device Configuration Incompatible",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DayTextPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "SilentBeacon requires offline peer-to-peer radio chipsets (BLE & Wi-Fi Direct) and Google Play Services to safely create a phone-to-phone disaster mesh. Your device does not meet these minimum requirements.",
                    fontSize = 13.sp,
                    color = DayTextSecondary,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )

                // Device Info Chip
                Card(
                    colors = CardDefaults.cardColors(containerColor = DaySurface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DayBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = DayTextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = report.deviceModel,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = DayTextPrimary
                            )
                            Text(
                                text = "${report.androidReleaseName} ${if (report.isAndroidGoOrLowRam) "• Android Go Edition" else ""}",
                                fontSize = 12.sp,
                                color = DayTextSecondary
                            )
                        }
                    }
                }

                // Fatal Issues Card
                if (report.fatalIssues.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmergencyRedLight),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, EmergencyRed.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Missing Mandatory Capabilities",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = EmergencyRed
                            )
                            report.fatalIssues.forEach { issue ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = EmergencyRed,
                                        modifier = Modifier.size(18.dp).padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = issue.title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = DayTextPrimary)
                                        Text(text = issue.description, fontSize = 11.sp, color = DayTextSecondary, lineHeight = 15.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Complete Diagnostics Checklist
                Card(
                    colors = CardDefaults.cardColors(containerColor = DaySurface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DayBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Hardware & Software Diagnostic Report",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = DayTextPrimary
                        )

                        report.diagnostics.forEach { diag ->
                            DiagnosticRow(diag)
                        }
                    }
                }

                // Re-check button
                Button(
                    onClick = onRecheck,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("recheck_compatibility_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ActionBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Re-scan Device Radios", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = White)
                }

                // Offline Emergency Survival Guide (Always useful!)
                Card(
                    colors = CardDefaults.cardColors(containerColor = DaySurfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DayBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = ActionBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Offline Disaster Survival Guide",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = DayTextPrimary
                            )
                        }

                        Text(
                            text = "Even if mesh radios are disabled on this device, remember these standard emergency procedures:",
                            fontSize = 11.sp,
                            color = DayTextSecondary,
                            lineHeight = 15.sp
                        )

                        SurvivalTipItem(
                            code = "S-O-S Audio Signal",
                            desc = "3 short whistle blasts, 3 long blasts, 3 short blasts. Repeat every 1 minute."
                        )
                        SurvivalTipItem(
                            code = "Flashlight Signal",
                            desc = "3 quick flashes, 3 long flashes, 3 quick flashes pointed toward the sky or rescue teams."
                        )
                        SurvivalTipItem(
                            code = "Civil Protection Frequency",
                            desc = "Keep an analog FM radio tuned to 88.1 - 92.5 MHz for national emergency civil broadcasts."
                        )
                        SurvivalTipItem(
                            code = "Water Rationing",
                            desc = "Minimum 1 gallon per person per day. Never drink untreated flood water."
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DiagnosticRow(diag: FeatureDiagnostic) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = diag.name,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = DayTextPrimary
            )
            Text(
                text = diag.details,
                fontSize = 11.sp,
                color = DayTextSecondary
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .background(
                    if (diag.isSupported) CalmGreenLight else if (diag.isMandatory) EmergencyRedLight else SafetyAmberLight,
                    RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (diag.isSupported) "READY" else if (diag.isMandatory) "MISSING" else "LIMITED",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (diag.isSupported) CalmGreen else if (diag.isMandatory) EmergencyRed else SafetyAmber
            )
        }
    }
}

@Composable
fun SurvivalTipItem(code: String, desc: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DaySurface, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Text(text = code, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = DayTextPrimary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = desc, fontSize = 11.sp, color = DayTextSecondary, lineHeight = 15.sp)
    }
}

@Composable
fun PermissionRequestScreen(
    report: DeviceCompatibilityReport,
    onRequestPermissions: () -> Unit,
    onProceedAnyway: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DayBackground
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .background(CalmGreenLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Podcasts,
                        contentDescription = null,
                        tint = CalmGreen,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Welcome to SilentBeacon",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = DayTextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Your device (${report.deviceModel}) is compatible with offline mesh radios. Connect directly with nearby phones in blackouts when cellular towers are down.",
                    fontSize = 13.sp,
                    color = DayTextSecondary,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )

                if (report.isAndroidGoOrLowRam) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SafetyAmberLight),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = SafetyAmber, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Android Go device detected: Power-saver mode active to keep memory minimal.",
                                fontSize = 11.sp,
                                color = DayTextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = DaySurface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, DayBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        FriendlyPermissionItem(
                            icon = Icons.Default.WifiTethering,
                            iconColor = CalmGreen,
                            title = "Nearby Bluetooth & Wi-Fi Direct",
                            desc = "Connects directly with nearby phones to pass emergency messages."
                        )
                        FriendlyPermissionItem(
                            icon = Icons.Default.LocationOn,
                            iconColor = SafetyAmber,
                            title = "Offline GPS Location",
                            desc = "Attaches your coordinates so rescue teams know where to find you."
                        )
                        FriendlyPermissionItem(
                            icon = Icons.Default.Security,
                            iconColor = ActionBlue,
                            title = "Background Relay Service",
                            desc = "Passes emergency alerts along even when your screen is locked."
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onRequestPermissions,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("grant_permissions_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Allow Offline Radio Access", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onProceedAnyway,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("preview_dashboard_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = DaySurface),
                    border = BorderStroke(1.dp, DayBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Open App Without Radios (Preview)", color = DayTextSecondary, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun FriendlyPermissionItem(icon: ImageVector, iconColor: Color, title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconColor.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DayTextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = desc, fontSize = 12.sp, color = DayTextSecondary, lineHeight = 16.sp)
        }
    }
}
