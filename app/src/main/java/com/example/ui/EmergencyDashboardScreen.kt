package com.example.ui

import android.location.Location
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EmergencyTicket
import com.example.data.model.MeshActivityLog
import com.example.data.model.TicketCategory
import com.example.data.model.TicketStatus
import com.example.data.model.UrgencyLevel
import com.example.ui.theme.ActionBlue
import com.example.ui.theme.ActionBlueLight
import com.example.ui.theme.CalmGreen
import com.example.ui.theme.CalmGreenLight
import com.example.ui.theme.CategoryHazard
import com.example.ui.theme.CategoryHazardBg
import com.example.ui.theme.CategoryMedical
import com.example.ui.theme.CategoryMedicalBg
import com.example.ui.theme.CategoryRescue
import com.example.ui.theme.CategoryRescueBg
import com.example.ui.theme.CategoryShelter
import com.example.ui.theme.CategoryShelterBg
import com.example.ui.theme.CategoryWaterFood
import com.example.ui.theme.CategoryWaterFoodBg
import com.example.ui.theme.DayBackground
import com.example.ui.theme.DayBorder
import com.example.ui.theme.DaySurface
import com.example.ui.theme.DaySurfaceVariant
import com.example.ui.theme.DayTextPrimary
import com.example.ui.theme.DayTextSecondary
import com.example.ui.theme.DayTextTertiary
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedHover
import com.example.ui.theme.EmergencyRedLight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.ui.components.VicinityRadarTab
import com.example.ui.components.ResolvedSolutionsTab
import com.example.ui.components.SurvivalGuideTab
import com.example.ui.theme.SafetyAmber
import com.example.ui.theme.SafetyAmberLight
import com.example.ui.theme.White
import com.example.ui.components.MeshSignalStrengthCard
import com.example.ui.components.SignalBarsWidget
import com.example.ui.components.TopBarSignalStrengthPill
import com.example.ui.components.calculateMeshSignalInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class DashboardTab(val label: String, val icon: ImageVector) {
    LIVE_SOS("Live SOS", Icons.Default.Podcasts),
    VICINITY_RADAR("Mesh Radar", Icons.Default.Sensors),
    RESOLVED_SOLUTIONS("Resolved", Icons.Default.CheckCircle),
    SURVIVAL_GUIDE("Survival Guide", Icons.Default.MenuBook)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyDashboardScreen(
    state: DashboardUiState,
    onDispatchTicket: (category: TicketCategory, urgency: UrgencyLevel, title: String, desc: String, locDesc: String, lat: Double?, lng: Double?) -> Unit,
    onResolveTicket: (ticketId: String) -> Unit,
    onAcknowledgeTicket: (ticketId: String) -> Unit,
    onToggleMesh: (Boolean) -> Unit,
    onPingHandshake: (endpointId: String) -> Unit,
    onFetchLocation: () -> Unit,
    onSetCategoryFilter: (TicketCategory?) -> Unit,
    onSetUrgencyFilter: (UrgencyLevel?) -> Unit,
    onSetStatusFilter: (TicketStatus?) -> Unit,
    onSetSearchQuery: (String) -> Unit,
    onSilenceAlarm: () -> Unit,
    onTestAlarm: () -> Unit,
    onDeleteTicket: (String) -> Unit = {},
    onClearAllResolved: () -> Unit = {},
    onSimulatePeer: () -> Unit = {},
    onDisconnectPeer: (String) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(DashboardTab.LIVE_SOS) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var showDispatchDialog by remember { mutableStateOf(false) }
    var showNetworkInfoDialog by remember { mutableStateOf(false) }
    var selectedTicketDetail by remember { mutableStateOf<EmergencyTicket?>(null) }
    var isSearchExpanded by remember { mutableStateOf(false) }

    val activeTickets = remember(state.filteredTickets) {
        state.filteredTickets.filter { it.status != TicketStatus.RESOLVED }
    }
    val resolvedTicketsCount = remember(state.tickets) {
        state.tickets.count { it.status == TicketStatus.RESOLVED }
    }
    val activeTicketsCount = remember(state.tickets) {
        state.tickets.count { it.status != TicketStatus.RESOLVED }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = DaySurface,
                modifier = Modifier.widthIn(max = 320.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Drawer Header
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(ActionBlueLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Podcasts,
                                contentDescription = null,
                                tint = ActionBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("SilentBeacon", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DayTextPrimary)
                            Text("Zero-Bandwidth Mesh", fontSize = 12.sp, color = DayTextSecondary)
                        }
                    }

                    // Node ID & Signal Summary Chip
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DaySurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DayBorder),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("DEVICE NODE ID", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DayTextSecondary)
                                Text(state.localNodeId.take(8), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DayTextPrimary, fontFamily = FontFamily.Monospace)
                            }
                            TopBarSignalStrengthPill(
                                connectedCount = state.connectedPeers.size,
                                isMeshActive = state.isMeshActive,
                                onClick = {
                                    selectedTab = DashboardTab.VICINITY_RADAR
                                    coroutineScope.launch { drawerState.close() }
                                }
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = DayBorder)

                    Text("NAVIGATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DayTextSecondary, letterSpacing = 0.5.sp)

                    DashboardTab.entries.forEach { tab ->
                        NavigationDrawerItem(
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(tab.label, fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium) },
                            selected = selectedTab == tab,
                            onClick = {
                                selectedTab = tab
                                coroutineScope.launch { drawerState.close() }
                            },
                            badge = {
                                when (tab) {
                                    DashboardTab.LIVE_SOS -> if (activeTicketsCount > 0) {
                                        Surface(shape = CircleShape, color = EmergencyRed) {
                                            Text("$activeTicketsCount", color = White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                    DashboardTab.VICINITY_RADAR -> if (state.connectedPeers.isNotEmpty()) {
                                        Surface(shape = CircleShape, color = CalmGreen) {
                                            Text("${state.connectedPeers.size}", color = White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                    DashboardTab.RESOLVED_SOLUTIONS -> if (resolvedTicketsCount > 0) {
                                        Surface(shape = CircleShape, color = DaySurfaceVariant) {
                                            Text("$resolvedTicketsCount", color = DayTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                    else -> {}
                                }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = ActionBlueLight,
                                selectedIconColor = ActionBlue,
                                selectedTextColor = ActionBlue,
                                unselectedIconColor = DayTextSecondary,
                                unselectedTextColor = DayTextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = DayBorder)

                    Text("QUICK TOOLS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DayTextSecondary, letterSpacing = 0.5.sp)

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = EmergencyRed) },
                        label = { Text("Sound Siren Drill", color = EmergencyRed, fontWeight = FontWeight.Bold) },
                        selected = false,
                        onClick = {
                            onTestAlarm()
                            coroutineScope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = EmergencyRedLight),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f, fill = false))

                    // Bottom Drawer Footer
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DaySurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Mesh Radios", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DayTextPrimary)
                                Text(if (state.isMeshActive) "BLE & Wi-Fi Active" else "Offline", fontSize = 11.sp, color = if (state.isMeshActive) CalmGreen else DayTextSecondary)
                            }
                            Switch(
                                checked = state.isMeshActive,
                                onCheckedChange = { onToggleMesh(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = White, checkedTrackColor = CalmGreen)
                            )
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("dashboard_scaffold"),
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = { coroutineScope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("hamburger_menu_button")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Navigation Menu", tint = DayTextPrimary)
                        }
                    },
                    title = {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = when (selectedTab) {
                                        DashboardTab.LIVE_SOS -> "SilentBeacon"
                                        DashboardTab.VICINITY_RADAR -> "Vicinity Radar"
                                        DashboardTab.RESOLVED_SOLUTIONS -> "Resolved Archive"
                                        DashboardTab.SURVIVAL_GUIDE -> "Survival Guide"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = DayTextPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                DayPulseIndicator(isActive = state.isMeshActive && state.connectedPeers.isNotEmpty())
                            }
                            Text(
                                text = when (selectedTab) {
                                    DashboardTab.LIVE_SOS -> "Offline Emergency Mesh"
                                    DashboardTab.VICINITY_RADAR -> "${state.connectedPeers.size} Connected Nodes in Range"
                                    DashboardTab.RESOLVED_SOLUTIONS -> "$resolvedTicketsCount Solved Situations"
                                    DashboardTab.SURVIVAL_GUIDE -> "Disaster Protocols & Signals"
                                },
                                fontSize = 11.sp,
                                color = DayTextSecondary
                            )
                        }
                    },
                    actions = {
                        // Visual signal strength indicator pill
                        TopBarSignalStrengthPill(
                            connectedCount = state.connectedPeers.size,
                            isMeshActive = state.isMeshActive,
                            onClick = { selectedTab = DashboardTab.VICINITY_RADAR },
                            modifier = Modifier.padding(end = 4.dp)
                        )

                        // Mesh radio switch
                        Switch(
                            checked = state.isMeshActive,
                            onCheckedChange = { onToggleMesh(it) },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("mesh_power_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = White,
                                checkedTrackColor = CalmGreen,
                                uncheckedThumbColor = DayTextTertiary,
                                uncheckedTrackColor = DayBorder
                            )
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DaySurface,
                        titleContentColor = DayTextPrimary
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = DaySurface,
                    tonalElevation = 3.dp,
                    modifier = Modifier.testTag("dashboard_bottom_bar")
                ) {
                    DashboardTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = {
                                when (tab) {
                                    DashboardTab.LIVE_SOS -> {
                                        if (activeTicketsCount > 0) {
                                            BadgedBox(badge = { Badge { Text("$activeTicketsCount") } }) {
                                                Icon(tab.icon, contentDescription = tab.label)
                                            }
                                        } else {
                                            Icon(tab.icon, contentDescription = tab.label)
                                        }
                                    }
                                    DashboardTab.VICINITY_RADAR -> {
                                        if (state.connectedPeers.isNotEmpty()) {
                                            BadgedBox(badge = { Badge { Text("${state.connectedPeers.size}") } }) {
                                                Icon(tab.icon, contentDescription = tab.label)
                                            }
                                        } else {
                                            Icon(tab.icon, contentDescription = tab.label)
                                        }
                                    }
                                    DashboardTab.RESOLVED_SOLUTIONS -> {
                                        if (resolvedTicketsCount > 0) {
                                            BadgedBox(badge = { Badge { Text("$resolvedTicketsCount") } }) {
                                                Icon(tab.icon, contentDescription = tab.label)
                                            }
                                        } else {
                                            Icon(tab.icon, contentDescription = tab.label)
                                        }
                                    }
                                    else -> Icon(tab.icon, contentDescription = tab.label)
                                }
                            },
                            label = { Text(tab.label, fontSize = 11.sp, fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ActionBlue,
                                selectedTextColor = ActionBlue,
                                indicatorColor = ActionBlueLight,
                                unselectedIconColor = DayTextSecondary,
                                unselectedTextColor = DayTextSecondary
                            )
                        )
                    }
                }
            },
            floatingActionButton = {
                if (selectedTab == DashboardTab.LIVE_SOS) {
                    FloatingActionButton(
                        onClick = { showDispatchDialog = true },
                        containerColor = EmergencyRed,
                        contentColor = White,
                        shape = RoundedCornerShape(20.dp),
                        elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(6.dp),
                        modifier = Modifier
                            .testTag("broadcast_sos_fab")
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CrisisAlert,
                                contentDescription = "Ask for Help",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "ASK FOR HELP",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(DayBackground)
            ) {
                when (selectedTab) {
                    DashboardTab.LIVE_SOS -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .widthIn(max = 680.dp)
                                .align(Alignment.TopCenter)
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("tickets_feed_list"),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 110.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // 1. Visual Signal Strength & Immediate Vicinity Node Density Card
                                item {
                                    MeshSignalStrengthCard(
                                        state = state,
                                        onOpenNetworkDetails = { selectedTab = DashboardTab.VICINITY_RADAR }
                                    )
                                }

                                // 2. Hero Emergency Card (Easy for anyone to tap immediately)
                                item {
                                    EmergencyHeroCard(
                                        onAskForHelp = { showDispatchDialog = true },
                                        onCheckGps = onFetchLocation,
                                        lastKnownLocation = state.lastKnownLocation,
                                        isLocating = state.isLocating
                                    )
                                }

                                // 3. Simple Clean Filters
                                item {
                                    SimplifiedFiltersSection(
                                        state = state,
                                        isSearchExpanded = isSearchExpanded,
                                        onToggleSearch = { isSearchExpanded = !isSearchExpanded },
                                        onSetCategory = onSetCategoryFilter,
                                        onSetUrgency = onSetUrgencyFilter,
                                        onSetStatus = onSetStatusFilter,
                                        onSetSearchQuery = onSetSearchQuery
                                    )
                                }

                                // 4. Feed Section Title: Showing ONLY Active Help Requests
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "ACTIVE CALLS FOR HELP (${activeTickets.size})",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DayTextSecondary,
                                            letterSpacing = 0.5.sp
                                        )

                                        if (state.connectedPeers.isNotEmpty()) {
                                            Text(
                                                text = "Syncing with ${state.connectedPeers.size} node(s)",
                                                fontSize = 12.sp,
                                                color = CalmGreen,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                // 5. Incident List Items or Empty View
                                if (activeTickets.isEmpty()) {
                                    item {
                                        FriendlyEmptyState(
                                            isMeshActive = state.isMeshActive,
                                            peerCount = state.connectedPeers.size,
                                            hasFilter = state.selectedCategoryFilter != null || state.selectedUrgencyFilter != null || state.searchQuery.isNotBlank(),
                                            onAskForHelp = { showDispatchDialog = true },
                                            onClearFilters = {
                                                onSetCategoryFilter(null)
                                                onSetUrgencyFilter(null)
                                                onSetStatusFilter(null)
                                                onSetSearchQuery("")
                                            }
                                        )
                                    }
                                } else {
                                    items(activeTickets, key = { it.ticketId }) { ticket ->
                                        FriendlyEmergencyCard(
                                            ticket = ticket,
                                            isOwnTicket = ticket.authorNodeId == state.localNodeId,
                                            onCardClick = { selectedTicketDetail = ticket },
                                            onAcknowledge = { onAcknowledgeTicket(ticket.ticketId) },
                                            onResolve = { onResolveTicket(ticket.ticketId) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    DashboardTab.VICINITY_RADAR -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .widthIn(max = 680.dp)
                                .align(Alignment.TopCenter)
                        ) {
                            VicinityRadarTab(
                                state = state,
                                onPingPeer = onPingHandshake,
                                onSimulatePeer = onSimulatePeer,
                                onDisconnectPeer = onDisconnectPeer
                            )
                        }
                    }

                    DashboardTab.RESOLVED_SOLUTIONS -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .widthIn(max = 680.dp)
                                .align(Alignment.TopCenter)
                        ) {
                            ResolvedSolutionsTab(
                                state = state,
                                onDeleteTicket = onDeleteTicket,
                                onClearAllResolved = onClearAllResolved,
                                onTicketClick = { selectedTicketDetail = it }
                            )
                        }
                    }

                    DashboardTab.SURVIVAL_GUIDE -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .widthIn(max = 680.dp)
                                .align(Alignment.TopCenter)
                        ) {
                            SurvivalGuideTab(
                                onTestAlarm = onTestAlarm
                            )
                        }
                    }
                }
            }

            // Dialogs
            if (state.isAlarmRinging && state.activeAlarmTicket != null) {
                CriticalAlarmSirenOverlay(
                    ticket = state.activeAlarmTicket,
                    onSilence = onSilenceAlarm,
                    onViewDetails = {
                        onSilenceAlarm()
                        selectedTicketDetail = state.activeAlarmTicket
                    }
                )
            }

            if (showDispatchDialog) {
                SimpleDispatchDialog(
                    lastKnownLocation = state.lastKnownLocation,
                    isLocating = state.isLocating,
                    onRefreshGps = onFetchLocation,
                    onDismiss = { showDispatchDialog = false },
                    onSubmit = { cat, urg, title, desc, locDesc, lat, lng ->
                        onDispatchTicket(cat, urg, title, desc, locDesc, lat, lng)
                        showDispatchDialog = false
                    }
                )
            }

            if (showNetworkInfoDialog) {
                FriendlyNetworkDialog(
                    state = state,
                    onDismiss = { showNetworkInfoDialog = false },
                    onPingPeer = onPingHandshake,
                    onTestAlarm = onTestAlarm
                )
            }

            selectedTicketDetail?.let { ticket ->
                FriendlyTicketDetailDialog(
                    ticket = ticket,
                    localNodeId = state.localNodeId,
                    onDismiss = { selectedTicketDetail = null },
                    onAcknowledge = {
                        onAcknowledgeTicket(ticket.ticketId)
                        selectedTicketDetail = null
                    },
                    onResolve = {
                        onResolveTicket(ticket.ticketId)
                        selectedTicketDetail = null
                    },
                    onDelete = {
                        onDeleteTicket(ticket.ticketId)
                        selectedTicketDetail = null
                    }
                )
            }
        }
    }
}

@Composable
fun DayPulseIndicator(isActive: Boolean) {
    if (!isActive) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(SafetyAmber, CircleShape)
        )
        return
    }

    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = Modifier
            .size(10.dp)
            .scale(scale)
            .background(CalmGreen, CircleShape)
    )
}

@Composable
fun EmergencyHeroCard(
    onAskForHelp: () -> Unit,
    onCheckGps: () -> Unit,
    lastKnownLocation: Location?,
    isLocating: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = EmergencyRedLight),
        border = androidx.compose.foundation.BorderStroke(1.dp, EmergencyRed.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(EmergencyRed, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CrisisAlert,
                            contentDescription = null,
                            tint = White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Are you or someone in danger?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = EmergencyRedHover
                        )
                        Text(
                            text = "Works without phone signal or internet",
                            fontSize = 12.sp,
                            color = DayTextSecondary
                        )
                    }
                }
            }

            Button(
                onClick = onAskForHelp,
                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("hero_sos_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("SEND EMERGENCY SOS", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = White)
            }

            // Location status pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCheckGps() }
                    .background(White, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (lastKnownLocation != null) Icons.Default.MyLocation else Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (lastKnownLocation != null) CalmGreen else DayTextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (lastKnownLocation != null) {
                            "GPS Active (±%.0fm)".format(lastKnownLocation.accuracy)
                        } else {
                            "Offline GPS (tap to update)"
                        },
                        fontSize = 12.sp,
                        color = DayTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (isLocating) {
                    CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = ActionBlue)
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = "Update GPS", tint = ActionBlue, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SimplifiedFiltersSection(
    state: DashboardUiState,
    isSearchExpanded: Boolean,
    onToggleSearch: () -> Unit,
    onSetCategory: (TicketCategory?) -> Unit,
    onSetUrgency: (UrgencyLevel?) -> Unit,
    onSetStatus: (TicketStatus?) -> Unit,
    onSetSearchQuery: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Search bar
        if (isSearchExpanded || state.searchQuery.isNotBlank()) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSetSearchQuery,
                placeholder = { Text("Search by landmark, title, or item...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DayTextSecondary) },
                trailingIcon = {
                    IconButton(onClick = {
                        onSetSearchQuery("")
                        onToggleSearch()
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = DayTextSecondary)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_field"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ActionBlue,
                    unfocusedBorderColor = DayBorder,
                    focusedContainerColor = White,
                    unfocusedContainerColor = White,
                    focusedTextColor = DayTextPrimary,
                    unfocusedTextColor = DayTextPrimary
                )
            )
        }

        // Top Status Tabs: All, Needs Help, Resolved
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val allSelected = state.selectedStatusFilter == null && state.selectedUrgencyFilter == null
            val activeSelected = state.selectedStatusFilter == TicketStatus.OPEN
            val resolvedSelected = state.selectedStatusFilter == TicketStatus.RESOLVED

            StatusTabPill(
                label = "All Alerts",
                isSelected = allSelected,
                count = state.tickets.size,
                modifier = Modifier.weight(1f),
                onClick = {
                    onSetStatus(null)
                    onSetUrgency(null)
                }
            )

            StatusTabPill(
                label = "Active Help",
                isSelected = activeSelected,
                count = state.tickets.count { it.status != TicketStatus.RESOLVED },
                selectedColor = EmergencyRed,
                selectedBg = EmergencyRedLight,
                modifier = Modifier.weight(1f),
                onClick = {
                    onSetStatus(if (activeSelected) null else TicketStatus.OPEN)
                    onSetUrgency(null)
                }
            )

            StatusTabPill(
                label = "Resolved",
                isSelected = resolvedSelected,
                count = state.tickets.count { it.status == TicketStatus.RESOLVED },
                selectedColor = CalmGreen,
                selectedBg = CalmGreenLight,
                modifier = Modifier.weight(1f),
                onClick = {
                    onSetStatus(if (resolvedSelected) null else TicketStatus.RESOLVED)
                    onSetUrgency(null)
                }
            )

            // Search Icon button
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSearchExpanded || state.searchQuery.isNotBlank()) ActionBlueLight else White,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSearchExpanded || state.searchQuery.isNotBlank()) ActionBlue else DayBorder),
                modifier = Modifier.clickable { onToggleSearch() }
            ) {
                Box(modifier = Modifier.padding(10.dp)) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = if (isSearchExpanded || state.searchQuery.isNotBlank()) ActionBlue else DayTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Horizontal Category Row
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TicketCategory.entries.forEach { cat ->
                val isSelected = state.selectedCategoryFilter == cat
                val catColor = getCategoryThemeColor(cat)
                val catBg = getCategoryThemeBg(cat)

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) catColor else White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) catColor else DayBorder),
                    modifier = Modifier.clickable { onSetCategory(if (isSelected) null else cat) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(cat),
                            contentDescription = null,
                            tint = if (isSelected) White else catColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = getCategoryDisplayName(cat),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) White else DayTextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusTabPill(
    label: String,
    isSelected: Boolean,
    count: Int,
    modifier: Modifier = Modifier,
    selectedColor: Color = ActionBlue,
    selectedBg: Color = ActionBlueLight,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) selectedBg else White,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) selectedColor else DayBorder),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) selectedColor else DayTextSecondary,
                maxLines = 1
            )
            Text(
                text = "$count",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = if (isSelected) selectedColor else DayTextPrimary
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FriendlyEmergencyCard(
    ticket: EmergencyTicket,
    isOwnTicket: Boolean,
    onCardClick: () -> Unit,
    onAcknowledge: () -> Unit,
    onResolve: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val isResolved = ticket.status == TicketStatus.RESOLVED
    val isAcknowledged = ticket.status == TicketStatus.ACKNOWLEDGED
    val catColor = getCategoryThemeColor(ticket.category)
    val catBg = getCategoryThemeBg(ticket.category)

    val urgencyBg = when (ticket.urgency) {
        UrgencyLevel.CRITICAL -> EmergencyRedLight
        UrgencyLevel.HIGH -> SafetyAmberLight
        UrgencyLevel.MEDIUM -> DaySurfaceVariant
        UrgencyLevel.LOW -> CalmGreenLight
    }

    val urgencyColor = when (ticket.urgency) {
        UrgencyLevel.CRITICAL -> EmergencyRed
        UrgencyLevel.HIGH -> SafetyAmber
        UrgencyLevel.MEDIUM -> DayTextSecondary
        UrgencyLevel.LOW -> CalmGreen
    }

    val urgencyLabel = when (ticket.urgency) {
        UrgencyLevel.CRITICAL -> "CRITICAL EMERGENCY"
        UrgencyLevel.HIGH -> "HIGH PRIORITY"
        UrgencyLevel.MEDIUM -> "HELP NEEDED"
        UrgencyLevel.LOW -> "GENERAL ASSISTANCE"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isResolved) DaySurfaceVariant else White),
        border = androidx.compose.foundation.BorderStroke(
            width = if (ticket.urgency == UrgencyLevel.CRITICAL && !isResolved) 1.5.dp else 1.dp,
            color = if (ticket.urgency == UrgencyLevel.CRITICAL && !isResolved) EmergencyRed.copy(alpha = 0.5f) else DayBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isResolved) 0.dp else 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("ticket_card_${ticket.ticketId}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Tags: Urgency & Category wrapped smoothly with FlowRow (won't crop on small phones)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Urgency Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = urgencyBg
                ) {
                    Text(
                        text = urgencyLabel,
                        color = urgencyColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Category Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = catBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(ticket.category),
                            contentDescription = null,
                            tint = catColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = getCategoryDisplayName(ticket.category),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = catColor
                        )
                    }
                }

                // Relative Time
                Text(
                    text = "• ${formatRelativeTime(ticket.createdAtEpochMs)}",
                    fontSize = 11.sp,
                    color = DayTextTertiary,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }

            // Title
            Text(
                text = ticket.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = if (isResolved) DayTextSecondary else DayTextPrimary,
                lineHeight = 22.sp
            )

            // Description
            if (ticket.description.isNotBlank()) {
                Text(
                    text = ticket.description,
                    fontSize = 14.sp,
                    color = DayTextSecondary,
                    lineHeight = 20.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Location Box (Clear, readable, comfortable)
            Surface(
                color = DaySurfaceVariant,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, DayBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = SafetyAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = ticket.locationDescription,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DayTextPrimary
                        )
                        if (ticket.latitude != null && ticket.longitude != null) {
                            Text(
                                text = "GPS: %.4f, %.4f".format(ticket.latitude, ticket.longitude),
                                fontSize = 11.sp,
                                color = DayTextSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Transmission info & status in plain language
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isOwnTicket) {
                        "📡 Sent from your device"
                    } else if (ticket.hopCount == 0) {
                        "📡 Direct signal"
                    } else {
                        "📡 Relayed via ${ticket.hopCount} nearby device(s)"
                    },
                    fontSize = 12.sp,
                    color = DayTextSecondary
                )

                if (isResolved) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CalmGreen, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Resolved", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CalmGreen)
                    }
                } else if (isAcknowledged) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Handshake, contentDescription = null, tint = ActionBlue, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Help is responding", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ActionBlue)
                    }
                }
            }

            // Action Buttons
            if (!isResolved) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isAcknowledged) {
                        OutlinedButton(
                            onClick = onAcknowledge,
                            modifier = Modifier
                                .height(40.dp)
                                .testTag("ack_button_${ticket.ticketId}"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ActionBlue),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ActionBlue)
                        ) {
                            Icon(Icons.Default.Handshake, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("I Can Help", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Button(
                        onClick = onResolve,
                        modifier = Modifier
                            .height(40.dp)
                            .testTag("resolve_button_${ticket.ticketId}"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CalmGreen)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mark Resolved", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = White)
                    }
                }
            } else if (onDelete != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDelete,
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("delete_resolved_button_${ticket.ticketId}"),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmergencyRedLight)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Resolved Solution",
                            modifier = Modifier.size(16.dp),
                            tint = EmergencyRed
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete Solution", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmergencyRed)
                    }
                }
            }
        }
    }
}

@Composable
fun FriendlyEmptyState(
    isMeshActive: Boolean,
    peerCount: Int,
    hasFilter: Boolean,
    onAskForHelp: () -> Unit,
    onClearFilters: () -> Unit
) {
    Surface(
        color = White,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DayBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(if (hasFilter) ActionBlueLight else CalmGreenLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (hasFilter) Icons.Default.Search else Icons.Default.Podcasts,
                    contentDescription = null,
                    tint = if (hasFilter) ActionBlue else CalmGreen,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (hasFilter) "No Matching Alerts" else "Radio Mesh Is Active",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = DayTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (hasFilter) {
                    "No requests found with your current filters or search terms."
                } else if (!isMeshActive) {
                    "Radio mesh is currently switched off. Turn on the switch at the top to connect with devices nearby."
                } else if (peerCount == 0) {
                    "Your device is ready and scanning. Any nearby phone with SilentBeacon will automatically connect and share emergency calls."
                } else {
                    "Connected to $peerCount phone(s) nearby. No active emergency calls have been posted in this area."
                },
                fontSize = 13.sp,
                color = DayTextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            if (hasFilter) {
                OutlinedButton(
                    onClick = onClearFilters,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Show All Alerts")
                }
            } else {
                Button(
                    onClick = onAskForHelp,
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Broadcast an SOS", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SimpleDispatchDialog(
    lastKnownLocation: Location?,
    isLocating: Boolean,
    onRefreshGps: () -> Unit,
    onDismiss: () -> Unit,
    onSubmit: (TicketCategory, UrgencyLevel, String, String, String, Double?, Double?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var locationDesc by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TicketCategory.MEDICAL) }
    var selectedUrgency by remember { mutableStateOf(UrgencyLevel.CRITICAL) }
    var attachGps by remember { mutableStateOf(true) }

    var titleError by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CrisisAlert,
                    contentDescription = null,
                    tint = EmergencyRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Broadcast Emergency Alert",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = DayTextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "This will broadcast immediately to all nearby devices within radio range without cellular signal or Wi-Fi.",
                    fontSize = 12.sp,
                    color = DayTextSecondary,
                    lineHeight = 17.sp
                )

                // 1. Category Selection
                Text(
                    text = "1. WHAT KIND OF HELP DO YOU NEED?",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DayTextSecondary,
                    letterSpacing = 0.5.sp
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TicketCategory.entries.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        val catColor = getCategoryThemeColor(cat)
                        val catBg = getCategoryThemeBg(cat)

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) catColor else catBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) catColor else DayBorder),
                            modifier = Modifier.clickable { selectedCategory = cat }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(cat),
                                    contentDescription = null,
                                    tint = if (isSelected) White else catColor,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = getCategoryDisplayName(cat),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) White else DayTextPrimary
                                )
                            }
                        }
                    }
                }

                // 2. Urgency level
                Text(
                    text = "2. HOW URGENT IS IT?",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DayTextSecondary,
                    letterSpacing = 0.5.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    UrgencyLevel.entries.forEach { level ->
                        val isSelected = selectedUrgency == level
                        val levelColor = when (level) {
                            UrgencyLevel.CRITICAL -> EmergencyRed
                            UrgencyLevel.HIGH -> SafetyAmber
                            UrgencyLevel.MEDIUM -> ActionBlue
                            UrgencyLevel.LOW -> CalmGreen
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) levelColor else DaySurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) levelColor else DayBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedUrgency = level }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = level.name,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) White else DayTextSecondary
                                )
                            }
                        }
                    }
                }

                // 3. What is needed (Title)
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = false
                    },
                    label = { Text("What is needed? *") },
                    placeholder = { Text("e.g. Critical: Insulin needed urgently") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sos_title_input"),
                    isError = titleError,
                    supportingText = if (titleError) {
                        { Text("Please enter what is needed", color = EmergencyRed) }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ActionBlue,
                        unfocusedBorderColor = DayBorder,
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedTextColor = DayTextPrimary,
                        unfocusedTextColor = DayTextPrimary
                    )
                )

                // 4. Details
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Extra details (optional)") },
                    placeholder = { Text("e.g. 2 adults, 1 infant trapped on 2nd floor, no power") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sos_details_input"),
                    minLines = 2,
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ActionBlue,
                        unfocusedBorderColor = DayBorder,
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedTextColor = DayTextPrimary,
                        unfocusedTextColor = DayTextPrimary
                    )
                )

                // 5. Landmark / Location
                OutlinedTextField(
                    value = locationDesc,
                    onValueChange = {
                        locationDesc = it
                        if (it.isNotBlank()) locationError = false
                    },
                    label = { Text("Where are you? (Landmark) *") },
                    placeholder = { Text("e.g. Sector 4, Community Hall Roof") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sos_location_input"),
                    isError = locationError,
                    supportingText = if (locationError) {
                        { Text("A landmark is critical for responders to find you", color = EmergencyRed) }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ActionBlue,
                        unfocusedBorderColor = DayBorder,
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedTextColor = DayTextPrimary,
                        unfocusedTextColor = DayTextPrimary
                    )
                )

                // 6. GPS status
                Surface(
                    color = DaySurfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DayBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (lastKnownLocation != null) Icons.Default.MyLocation else Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (lastKnownLocation != null) CalmGreen else SafetyAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (lastKnownLocation != null) "GPS Coordinates Locked" else "Offline GPS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DayTextPrimary
                                )
                                Text(
                                    text = if (lastKnownLocation != null) {
                                        "Coordinates will be attached automatically"
                                    } else {
                                        "Responders will locate you by landmark"
                                    },
                                    fontSize = 11.sp,
                                    color = DayTextSecondary
                                )
                            }
                        }

                        IconButton(onClick = onRefreshGps) {
                            if (isLocating) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = ActionBlue)
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh GPS", tint = ActionBlue, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) titleError = true
                    if (locationDesc.isBlank()) locationError = true
                    if (title.isNotBlank() && locationDesc.isNotBlank()) {
                        val lat = if (attachGps) lastKnownLocation?.latitude else null
                        val lng = if (attachGps) lastKnownLocation?.longitude else null
                        onSubmit(selectedCategory, selectedUrgency, title, description, locationDesc, lat, lng)
                    }
                },
                modifier = Modifier.testTag("submit_sos_button"),
                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp), tint = White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Broadcast SOS", fontWeight = FontWeight.Bold, color = White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = DayTextSecondary)
            }
        },
        containerColor = White,
        textContentColor = DayTextPrimary,
        titleContentColor = DayTextPrimary
    )
}

@Composable
fun FriendlyNetworkDialog(
    state: DashboardUiState,
    onDismiss: () -> Unit,
    onPingPeer: (String) -> Unit,
    onTestAlarm: () -> Unit
) {
    var showTechDetails by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WifiTethering, contentDescription = null, tint = ActionBlue, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Device Network", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DayTextPrimary)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = DayTextSecondary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Explanation Card
                Surface(
                    color = ActionBlueLight,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ActionBlue.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "How this works",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ActionBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Your phone connects directly to nearby phones using Bluetooth and Wi-Fi Direct. When you send or receive an alert, phones relay the message automatically like a bucket brigade without cell towers.",
                            fontSize = 12.sp,
                            color = DayTextSecondary,
                            lineHeight = 17.sp
                        )
                    }
                }

                // Summary stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = DaySurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("NEARBY PHONES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DayTextSecondary)
                            Text("${state.connectedPeers.size}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (state.connectedPeers.isNotEmpty()) CalmGreen else DayTextPrimary)
                        }
                    }

                    Surface(
                        color = DaySurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("MESSAGES RELAYED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DayTextSecondary)
                            Text("${state.relayedPacketsCount}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = DayTextPrimary)
                        }
                    }
                }

                // Nearby Devices List
                Text(
                    text = "DEVICES IN RANGE (${state.connectedPeers.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DayTextSecondary,
                    letterSpacing = 0.5.sp
                )

                if (state.connectedPeers.isEmpty()) {
                    Surface(
                        color = DaySurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Scanning for nearby devices with SilentBeacon...\nKeep Bluetooth turned on. Connection happens automatically when devices come in range.",
                            fontSize = 12.sp,
                            color = DayTextSecondary,
                            lineHeight = 17.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else {
                    state.connectedPeers.forEachIndexed { index, endpointId ->
                        Surface(
                            color = White,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CalmGreen.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).background(CalmGreen, CircleShape))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Nearby Device #${index + 1}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DayTextPrimary)
                                        Text("Connected via Radio Link", fontSize = 11.sp, color = DayTextSecondary)
                                    }
                                }

                                OutlinedButton(
                                    onClick = { onPingPeer(endpointId) },
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Test Ping", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Test Siren Button
                Button(
                    onClick = onTestAlarm,
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRedLight),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmergencyRed.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = EmergencyRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🔊 Test Emergency Siren Sound",
                        color = EmergencyRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Expandable Technical Diagnostics Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTechDetails = !showTechDetails }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (showTechDetails) "Hide Technical Diagnostics" else "Show Technical Diagnostics",
                        fontSize = 12.sp,
                        color = ActionBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = ActionBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }

                if (showTechDetails) {
                    Surface(
                        color = DaySurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DayBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Node Identifier: #${state.localNodeId.uppercase()}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = DayTextPrimary)
                            Text("Lamport Clock: L=${state.lamportClock}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = DayTextPrimary)
                            Text("Total Packets Synced: ${state.syncedTicketsCount}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = DayTextPrimary)
                            Text("Service ID: silentbeacon.v1", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = DayTextPrimary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = ActionBlue, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = White,
        textContentColor = DayTextPrimary,
        titleContentColor = DayTextPrimary
    )
}

@Composable
fun FriendlyTicketDetailDialog(
    ticket: EmergencyTicket,
    localNodeId: String,
    onDismiss: () -> Unit,
    onAcknowledge: () -> Unit,
    onResolve: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val isResolved = ticket.status == TicketStatus.RESOLVED
    val isAcknowledged = ticket.status == TicketStatus.ACKNOWLEDGED
    val catColor = getCategoryThemeColor(ticket.category)
    val catBg = getCategoryThemeBg(ticket.category)

    var showTechData by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(6.dp), color = catBg) {
                        Box(modifier = Modifier.padding(6.dp)) {
                            Icon(imageVector = getCategoryIcon(ticket.category), contentDescription = null, tint = catColor, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(ticket.title, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = DayTextPrimary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Description
                Text(
                    text = ticket.description.ifBlank { "No extra details written." },
                    fontSize = 14.sp,
                    color = DayTextPrimary,
                    lineHeight = 20.sp
                )

                // Landmark & Location Box
                Surface(
                    color = DaySurfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DayBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = SafetyAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(ticket.locationDescription, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DayTextPrimary)
                        }
                        if (ticket.latitude != null && ticket.longitude != null) {
                            Text(
                                text = "Coordinates: %.5f, %.5f".format(ticket.latitude, ticket.longitude),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = DayTextSecondary
                            )
                        }
                    }
                }

                // Current Status indicator
                Surface(
                    color = if (isResolved) CalmGreenLight else if (isAcknowledged) ActionBlueLight else EmergencyRedLight,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isResolved) Icons.Default.CheckCircle else if (isAcknowledged) Icons.Default.Handshake else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isResolved) CalmGreen else if (isAcknowledged) ActionBlue else EmergencyRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when {
                                isResolved -> "Marked as resolved"
                                isAcknowledged -> "Someone is responding to help"
                                else -> "Waiting for responders"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isResolved) CalmGreen else if (isAcknowledged) ActionBlue else EmergencyRed
                        )
                    }
                }

                // Relayed info
                Text(
                    text = "📡 Sent on ${SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(ticket.createdAtEpochMs))}",
                    fontSize = 12.sp,
                    color = DayTextSecondary
                )

                // Optional tech details
                Text(
                    text = if (showTechData) "Hide Network Data" else "Show Network Data",
                    fontSize = 11.sp,
                    color = ActionBlue,
                    modifier = Modifier
                        .clickable { showTechData = !showTechData }
                        .padding(vertical = 4.dp)
                )

                if (showTechData) {
                    Surface(
                        color = DaySurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Ticket ID: ${ticket.ticketId.take(12)}...", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text("Author Node: ${ticket.authorNodeId}", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text("Logical Clock: L=${ticket.lamportClock}", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text("Hops Relayed: ${ticket.hopCount} of ${ticket.maxHops}", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isResolved && onDelete != null) {
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = EmergencyRedLight)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = EmergencyRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete Solution", color = EmergencyRed, fontWeight = FontWeight.Bold)
                    }
                } else if (!isResolved) {
                    if (!isAcknowledged) {
                        OutlinedButton(onClick = onAcknowledge) {
                            Text("I Can Help", color = ActionBlue)
                        }
                    }
                    Button(
                        onClick = onResolve,
                        colors = ButtonDefaults.buttonColors(containerColor = CalmGreen)
                    ) {
                        Text("Mark Resolved", color = White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = DayTextSecondary)
            }
        },
        containerColor = White,
        textContentColor = DayTextPrimary,
        titleContentColor = DayTextPrimary
    )
}

fun getCategoryIcon(category: TicketCategory): ImageVector {
    return when (category) {
        TicketCategory.MEDICAL -> Icons.Default.MedicalServices
        TicketCategory.WATER_FOOD -> Icons.Default.WaterDrop
        TicketCategory.RESCUE -> Icons.Default.VolunteerActivism
        TicketCategory.SHELTER -> Icons.Default.Home
        TicketCategory.HAZARD -> Icons.Default.Warning
    }
}

fun getCategoryDisplayName(category: TicketCategory): String {
    return when (category) {
        TicketCategory.MEDICAL -> "Medical"
        TicketCategory.WATER_FOOD -> "Food & Water"
        TicketCategory.RESCUE -> "Rescue"
        TicketCategory.SHELTER -> "Shelter"
        TicketCategory.HAZARD -> "Hazard"
    }
}

fun getCategoryThemeColor(category: TicketCategory): Color {
    return when (category) {
        TicketCategory.MEDICAL -> CategoryMedical
        TicketCategory.WATER_FOOD -> CategoryWaterFood
        TicketCategory.RESCUE -> CategoryRescue
        TicketCategory.SHELTER -> CategoryShelter
        TicketCategory.HAZARD -> CategoryHazard
    }
}

fun getCategoryThemeBg(category: TicketCategory): Color {
    return when (category) {
        TicketCategory.MEDICAL -> CategoryMedicalBg
        TicketCategory.WATER_FOOD -> CategoryWaterFoodBg
        TicketCategory.RESCUE -> CategoryRescueBg
        TicketCategory.SHELTER -> CategoryShelterBg
        TicketCategory.HAZARD -> CategoryHazardBg
    }
}

fun formatRelativeTime(epochMs: Long): String {
    val diffSec = (System.currentTimeMillis() - epochMs) / 1000
    return when {
        diffSec < 60 -> "Just now"
        diffSec < 3600 -> "${diffSec / 60}m ago"
        diffSec < 86400 -> "${diffSec / 3600}h ago"
        else -> "${diffSec / 86400}d ago"
    }
}

@Composable
fun CriticalAlarmSirenOverlay(
    ticket: EmergencyTicket,
    onSilence: () -> Unit,
    onViewDetails: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "siren_pulse")
    val borderAlpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "siren_alpha"
    )

    AlertDialog(
        onDismissRequest = { /* Must be explicitly silenced or acknowledged */ },
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(EmergencyRed.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CrisisAlert,
                        contentDescription = "Siren Alert",
                        tint = EmergencyRed,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "🚨 CRITICAL EMERGENCY ALARM",
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    color = EmergencyRed,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "High priority SOS broadcast received nearby",
                    fontSize = 12.sp,
                    color = DayTextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Emergency Summary Box
                Surface(
                    color = EmergencyRedLight,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 2.dp,
                        color = EmergencyRed.copy(alpha = borderAlpha)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = ticket.title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmergencyRedHover
                        )

                        if (ticket.description.isNotBlank()) {
                            Text(
                                text = ticket.description,
                                fontSize = 13.sp,
                                color = DayTextPrimary,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = EmergencyRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = ticket.locationDescription,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DayTextPrimary
                            )
                        }
                    }
                }

                Text(
                    text = "Your phone's alarm is sounding to ensure this urgent life-safety broadcast is heard.",
                    fontSize = 11.sp,
                    color = DayTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onViewDetails,
                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("VIEW LOCATION & RESPOND", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onSilence,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, DayBorder),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.VolumeOff, contentDescription = null, tint = DayTextSecondary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("SILENCE ALARM", color = DayTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        },
        containerColor = White,
        textContentColor = DayTextPrimary,
        titleContentColor = DayTextPrimary
    )
}
