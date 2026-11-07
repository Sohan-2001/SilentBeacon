package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SafetyAmber
import com.example.ui.theme.White

class MainActivity : ComponentActivity() {

    private val viewModel: EmergencyDashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                    }
                }

                LaunchedEffect(Unit) {
                    if (!allPermissionsGranted) {
                        permissionLauncher.launch(requiredPermissions.toTypedArray())
                    } else {
                        viewModel.startMeshService()
                    }
                }

                if (!allPermissionsGranted) {
                    PermissionRequestScreen(
                        onRequestPermissions = {
                            permissionLauncher.launch(requiredPermissions.toTypedArray())
                        },
                        onProceedAnyway = {
                            allPermissionsGranted = true
                            viewModel.startMeshService()
                        }
                    )
                } else {
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
                        onTestAlarm = { viewModel.testEmergencyAlarm() }
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionRequestScreen(
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
                        .size(80.dp)
                        .background(EmergencyRed.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Podcasts,
                        contentDescription = null,
                        tint = EmergencyRed,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Welcome to SilentBeacon",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = DayTextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "This app works in blackouts and disaster zones when cellular towers and Wi-Fi networks are down. It connects your phone directly with other phones nearby.",
                    fontSize = 14.sp,
                    color = DayTextSecondary,
                    lineHeight = 20.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = DaySurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DayBorder),
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
                    border = androidx.compose.foundation.BorderStroke(1.dp, DayBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Open App Without Radios (Preview)", color = DayTextSecondary, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun FriendlyPermissionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color, title: String, desc: String) {
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
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DayTextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = desc, fontSize = 12.sp, color = DayTextSecondary, lineHeight = 16.sp)
        }
    }
}
