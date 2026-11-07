package com.example.ui.components

import android.location.Location
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import android.content.Intent
import android.provider.Settings
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Close
import com.example.network.MeshRadioStatus
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EmergencyTicket
import com.example.data.model.MeshActivityLog
import com.example.data.model.TicketCategory
import com.example.data.model.TicketStatus
import com.example.data.model.UrgencyLevel
import com.example.ui.DashboardUiState
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
import com.example.ui.theme.SafetyAmber
import com.example.ui.theme.SafetyAmberLight
import com.example.ui.theme.White
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Tab 2: Vicinity Radar & Node Connectivity Screen
 * Displays large constellation radar, connected nodes list, and live radio packet logs.
 */
@Composable
fun VicinityRadarTab(
    state: DashboardUiState,
    onPingPeer: (String) -> Unit,
    onSimulatePeer: () -> Unit = {},
    onDisconnectPeer: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("vicinity_radar_feed"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Radio State Alert (if Bluetooth is disabled in system settings)
        if (state.radioStatus == MeshRadioStatus.RADIO_DISABLED) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SafetyAmberLight),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SafetyAmber.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = SafetyAmber, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Bluetooth Radio is Turned Off", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DayTextPrimary)
                        }
                        Text(
                            text = "Turn on Bluetooth in system settings so nearby phone radios can discover your device and form the emergency mesh.",
                            fontSize = 12.sp,
                            color = DayTextSecondary,
                            lineHeight = 16.sp
                        )
                        Button(
                            onClick = {
                                try {
                                    context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                                } catch (e: Exception) {
                                    // Ignore
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Open Bluetooth Settings", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = White)
                        }
                    }
                }
            }
        } else if (state.radioStatus == MeshRadioStatus.LISTENER_ONLY) {
            item {
                Surface(
                    color = DaySurfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, DayBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Sensors, contentDescription = null, tint = ActionBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Listener/Scanner Mode Active: Can discover and link with all advertising mesh beacons.",
                            fontSize = 11.sp,
                            color = DayTextSecondary
                        )
                    }
                }
            }
        }

        // Visual Signal Strength & Node Density Radar Card
        item {
            MeshSignalStrengthCard(
                state = state,
                onOpenNetworkDetails = { /* Already in radar */ }
            )
        }

        // Summary Stats Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = DaySurface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DayBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("VICINITY NODES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DayTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${state.connectedPeers.size}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = if (state.connectedPeers.isNotEmpty()) CalmGreen else DayTextPrimary
                        )
                    }
                }

                Surface(
                    color = DaySurface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DayBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("RELAYED PACKETS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DayTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${state.relayedPacketsCount}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = DayTextPrimary
                        )
                    }
                }

                Surface(
                    color = DaySurface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DayBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("LAMPORT CLOCK", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DayTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${state.lamportClock}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = ActionBlue
                        )
                    }
                }
            }
        }

        // Section Title: Devices In Immediate Vicinity & Drill Simulation Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DEVICES IN VICINITY (${state.connectedPeers.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DayTextSecondary,
                    letterSpacing = 0.5.sp
                )

                OutlinedButton(
                    onClick = onSimulatePeer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(30.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    border = BorderStroke(1.dp, ActionBlue)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = ActionBlue)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Test Peer (Drill)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ActionBlue)
                }
            }
        }

        // Devices in Range List
        if (state.connectedPeers.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DaySurface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DayBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WifiTethering, contentDescription = null, tint = ActionBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Automatic Beacon Discovery", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DayTextPrimary)
                        }
                        Text(
                            text = "Radios are scanning via Bluetooth Low Energy and Wi-Fi Direct. When other devices running SilentBeacon enter your 30m–100m range, they connect automatically without internet.",
                            fontSize = 12.sp,
                            color = DayTextSecondary,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        } else {
            items(state.connectedPeers.toList()) { endpointId ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DaySurface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CalmGreen.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(CalmGreenLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                SignalBarsWidget(
                                    connectedCount = 3,
                                    isMeshActive = true,
                                    barWidth = 3.dp,
                                    maxHeight = 13.dp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Node ($endpointId)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = DayTextPrimary
                                )
                                Text(
                                    text = "Active Direct Radio Link",
                                    fontSize = 11.sp,
                                    color = CalmGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = { onPingPeer(endpointId) },
                                modifier = Modifier.height(32.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                border = BorderStroke(1.dp, ActionBlue)
                            ) {
                                Text("Test Ping", fontSize = 11.sp, color = ActionBlue, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { onDisconnectPeer(endpointId) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Disconnect Peer", tint = DayTextTertiary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Section Title: Live Mesh Activity Logs
        item {
            Text(
                text = "RADIO ACTIVITY LOG (${state.logs.size} EVENTS)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = DayTextSecondary,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (state.logs.isEmpty()) {
            item {
                Surface(
                    color = DaySurface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DayBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No radio packet events recorded yet. Radio handshake and sync packets will be logged here in real time.",
                        fontSize = 12.sp,
                        color = DayTextSecondary,
                        modifier = Modifier.padding(14.dp),
                        lineHeight = 16.sp
                    )
                }
            }
        } else {
            items(state.logs.take(25)) { log ->
                Surface(
                    color = DaySurface,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, DayBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestampMs)),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = DayTextTertiary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when {
                                log.isIncoming == true -> CalmGreenLight
                                log.isIncoming == false -> ActionBlueLight
                                else -> DaySurfaceVariant
                            }
                        ) {
                            Text(
                                text = log.tag,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    log.isIncoming == true -> CalmGreen
                                    log.isIncoming == false -> ActionBlue
                                    else -> DayTextSecondary
                                },
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = log.message,
                            fontSize = 11.sp,
                            color = DayTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tab 3: Resolved Solutions Screen (with DELETE options)
 * Lists all completed & resolved emergency cases, with individual delete and bulk clear actions.
 */
@Composable
fun ResolvedSolutionsTab(
    state: DashboardUiState,
    onDeleteTicket: (String) -> Unit,
    onClearAllResolved: () -> Unit,
    onTicketClick: (EmergencyTicket) -> Unit,
    modifier: Modifier = Modifier
) {
    val resolvedTickets = state.tickets.filter { it.status == TicketStatus.RESOLVED }
    var showClearAllDialog by remember { mutableStateOf(false) }
    var ticketToDelete by remember { mutableStateOf<EmergencyTicket?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("resolved_solutions_feed"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header Card with count & Clear All action
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DaySurface),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, DayBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(CalmGreenLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = CalmGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Resolved Solutions Archive",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = DayTextPrimary
                            )
                            Text(
                                text = "${resolvedTickets.size} ${if (resolvedTickets.size == 1) "situation" else "situations"} solved",
                                fontSize = 12.sp,
                                color = DayTextSecondary
                            )
                        }
                    }

                    if (resolvedTickets.isNotEmpty()) {
                        Button(
                            onClick = { showClearAllDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = EmergencyRedLight),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("clear_all_resolved_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Clear All Resolved",
                                tint = EmergencyRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear All", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmergencyRed)
                        }
                    }
                }
            }
        }

        // Empty state or Resolved Ticket list
        if (resolvedTickets.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DaySurface),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, DayBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(CalmGreenLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = CalmGreen,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "No Resolved Solutions Yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = DayTextPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "When active emergency calls are handled and marked as resolved, they will appear here so your main SOS feed remains clean and clutter-free.",
                            fontSize = 12.sp,
                            color = DayTextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        } else {
            items(resolvedTickets, key = { it.ticketId }) { ticket ->
                ResolvedSolutionCard(
                    ticket = ticket,
                    onCardClick = { onTicketClick(ticket) },
                    onDelete = { ticketToDelete = ticket }
                )
            }
        }
    }

    // Confirmation Dialog: Delete single resolved solution
    ticketToDelete?.let { ticket ->
        AlertDialog(
            onDismissRequest = { ticketToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = EmergencyRed, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Resolved Record?", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Text(
                    text = "Are you sure you want to delete '${ticket.title}' from your device records? This action cannot be undone.",
                    fontSize = 13.sp,
                    color = DayTextSecondary,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTicket(ticket.ticketId)
                        ticketToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold, color = White)
                }
            },
            dismissButton = {
                TextButton(onClick = { ticketToDelete = null }) {
                    Text("Cancel", color = DayTextSecondary)
                }
            },
            containerColor = DaySurface,
            textContentColor = DayTextPrimary,
            titleContentColor = DayTextPrimary
        )
    }

    // Confirmation Dialog: Clear ALL resolved solutions
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = EmergencyRed, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear All Resolved Solutions?", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Text(
                    text = "This will permanently remove all ${resolvedTickets.size} resolved emergency records from this device to clean your storage. Active distress calls will not be touched.",
                    fontSize = 13.sp,
                    color = DayTextSecondary,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllResolved()
                        showClearAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Clear All", fontWeight = FontWeight.Bold, color = White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel", color = DayTextSecondary)
                }
            },
            containerColor = DaySurface,
            textContentColor = DayTextPrimary,
            titleContentColor = DayTextPrimary
        )
    }
}

/**
 * Dedicated Card for Resolved Solutions with Delete option.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResolvedSolutionCard(
    ticket: EmergencyTicket,
    onCardClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DaySurface),
        border = BorderStroke(1.dp, CalmGreen.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .testTag("resolved_ticket_${ticket.ticketId}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Category, Resolved badge & Delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CalmGreenLight
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = CalmGreen, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("RESOLVED", color = CalmGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = ticket.category.name.replace("_", " "),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DayTextSecondary
                    )
                }

                // Delete Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("delete_button_${ticket.ticketId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Resolved Solution",
                        tint = EmergencyRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Title
            Text(
                text = ticket.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = DayTextPrimary
            )

            // Description
            if (ticket.description.isNotBlank()) {
                Text(
                    text = ticket.description,
                    fontSize = 13.sp,
                    color = DayTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }

            // Location & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = SafetyAmber, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = ticket.locationDescription,
                        fontSize = 11.sp,
                        color = DayTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(ticket.createdAtEpochMs)),
                    fontSize = 10.sp,
                    color = DayTextTertiary
                )
            }
        }
    }
}

/**
 * Tab 4: Survival Guide & Tools Screen
 * Contains offline disaster survival guidelines, S-O-S signaling protocols, and alarm testing.
 */
@Composable
fun SurvivalGuideTab(
    onTestAlarm: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("survival_guide_feed"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Banner Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ActionBlueLight),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, ActionBlue.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = ActionBlue, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Offline Disaster Manual", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ActionBlue)
                        Text("Standard safety & distress signaling protocols", fontSize = 12.sp, color = DayTextSecondary)
                    }
                }
            }
        }

        // Siren Alarm Drill Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DaySurface),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, DayBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Emergency Siren Drill", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DayTextPrimary)
                    Text(
                        text = "Verify that your speaker and vibration motors sound properly during critical broadcast alerts.",
                        fontSize = 12.sp,
                        color = DayTextSecondary,
                        lineHeight = 17.sp
                    )

                    Button(
                        onClick = onTestAlarm,
                        colors = ButtonDefaults.buttonColors(containerColor = EmergencyRedLight),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = EmergencyRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sound Emergency Siren Drill", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EmergencyRed)
                    }
                }
            }
        }

        // Standard Signaling Protocols
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DaySurface),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, DayBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Standard Distress Signals", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DayTextPrimary)

                    SurvivalProtocolItem(
                        title = "S-O-S Audio / Whistle Code",
                        desc = "3 short blasts, 3 long blasts, 3 short blasts (... --- ...). Pause 1 minute and repeat continuously."
                    )

                    SurvivalProtocolItem(
                        title = "Flashlight Rescue Signal",
                        desc = "3 quick flashes, 3 long flashes, 3 quick flashes pointed towards aircraft, ridgelines, or search parties."
                    )

                    SurvivalProtocolItem(
                        title = "National Emergency FM Radio",
                        desc = "Keep an analog battery or hand-crank radio tuned to 88.1 - 92.5 MHz for national civil defense announcements."
                    )

                    SurvivalProtocolItem(
                        title = "Water Purification & Rationing",
                        desc = "Minimum 1 gallon per person per day. Boil water vigorously for 1 full minute or use 2 drops of regular bleach per quart."
                    )
                }
            }
        }
    }
}

@Composable
fun SurvivalProtocolItem(title: String, desc: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DaySurfaceVariant, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DayTextPrimary)
        Spacer(modifier = Modifier.height(3.dp))
        Text(text = desc, fontSize = 11.sp, color = DayTextSecondary, lineHeight = 16.sp)
    }
}
