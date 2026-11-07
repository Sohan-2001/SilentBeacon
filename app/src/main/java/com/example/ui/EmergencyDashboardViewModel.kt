package com.example.ui

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SilentBeaconApp
import com.example.data.model.EmergencyTicket
import com.example.data.model.MeshActivityLog
import com.example.data.model.TicketCategory
import com.example.data.model.TicketStatus
import com.example.data.model.UrgencyLevel
import com.example.service.MeshRelayService
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val tickets: List<EmergencyTicket> = emptyList(),
    val filteredTickets: List<EmergencyTicket> = emptyList(),
    val connectedPeers: Set<String> = emptySet(),
    val isMeshActive: Boolean = false,
    val selectedCategoryFilter: TicketCategory? = null,
    val selectedUrgencyFilter: UrgencyLevel? = null,
    val selectedStatusFilter: TicketStatus? = null,
    val searchQuery: String = "",
    val localNodeId: String = "",
    val lamportClock: Long = 0L,
    val relayedPacketsCount: Int = 0,
    val syncedTicketsCount: Int = 0,
    val lastKnownLocation: Location? = null,
    val isLocating: Boolean = false,
    val logs: List<MeshActivityLog> = emptyList(),
    val isAlarmRinging: Boolean = false,
    val activeAlarmTicket: EmergencyTicket? = null
)

class EmergencyDashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SilentBeaconApp
    private val dao = app.database.ticketDao()
    private val meshManager = app.meshManager
    private val syncEngine = app.syncEngine
    private val alarmManager = app.alarmManager
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    private val _categoryFilter = MutableStateFlow<TicketCategory?>(null)
    private val _urgencyFilter = MutableStateFlow<UrgencyLevel?>(null)
    private val _statusFilter = MutableStateFlow<TicketStatus?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _lastKnownLocation = MutableStateFlow<Location?>(null)
    private val _isLocating = MutableStateFlow(false)
    private val _activityLogs = MutableStateFlow<List<MeshActivityLog>>(emptyList())

    val uiState: StateFlow<DashboardUiState> = combine(
        dao.observeAllTickets(),
        meshManager.connectedPeers,
        meshManager.isMeshActive,
        _categoryFilter,
        _urgencyFilter,
        _statusFilter,
        _searchQuery,
        syncEngine.relayedPacketsCount,
        syncEngine.syncedTicketsCount,
        _lastKnownLocation,
        _isLocating,
        _activityLogs,
        alarmManager.isAlarmActive,
        alarmManager.activeCriticalTicket
    ) { params ->
        @Suppress("UNCHECKED_CAST")
        val allTickets = params[0] as List<EmergencyTicket>
        @Suppress("UNCHECKED_CAST")
        val peers = params[1] as Set<String>
        val isMeshActive = params[2] as Boolean
        val catFilter = params[3] as TicketCategory?
        val urgFilter = params[4] as UrgencyLevel?
        val statusFilter = params[5] as TicketStatus?
        val query = params[6] as String
        val relays = params[7] as Int
        val synced = params[8] as Int
        val loc = params[9] as Location?
        val isLocating = params[10] as Boolean
        @Suppress("UNCHECKED_CAST")
        val logs = params[11] as List<MeshActivityLog>
        val isAlarmRinging = params[12] as Boolean
        val activeAlarmTicket = params[13] as EmergencyTicket?

        val filtered = allTickets.filter { ticket ->
            val matchesCat = catFilter == null || ticket.category == catFilter
            val matchesUrg = urgFilter == null || ticket.urgency == urgFilter
            val matchesStatus = statusFilter == null || ticket.status == statusFilter
            val matchesSearch = query.isBlank() ||
                    ticket.title.contains(query, ignoreCase = true) ||
                    ticket.description.contains(query, ignoreCase = true) ||
                    ticket.locationDescription.contains(query, ignoreCase = true) ||
                    ticket.ticketId.contains(query, ignoreCase = true)

            matchesCat && matchesUrg && matchesStatus && matchesSearch
        }

        DashboardUiState(
            tickets = allTickets,
            filteredTickets = filtered,
            connectedPeers = peers,
            isMeshActive = isMeshActive,
            selectedCategoryFilter = catFilter,
            selectedUrgencyFilter = urgFilter,
            selectedStatusFilter = statusFilter,
            searchQuery = query,
            localNodeId = app.localNodeId,
            lamportClock = syncEngine.getClock(),
            relayedPacketsCount = relays,
            syncedTicketsCount = synced,
            lastKnownLocation = loc,
            isLocating = isLocating,
            logs = logs,
            isAlarmRinging = isAlarmRinging,
            activeAlarmTicket = activeAlarmTicket
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(localNodeId = app.localNodeId)
    )

    init {
        // Collect radio activity logs
        viewModelScope.launch {
            meshManager.activityLogs.collect { newLog ->
                _activityLogs.value = (listOf(newLog) + _activityLogs.value).take(100)
            }
        }
        // Fetch initial GPS
        fetchCurrentLocation()
    }

    fun startMeshService() {
        MeshRelayService.startService(app)
        meshManager.startMesh()
    }

    fun stopMeshService() {
        meshManager.stopMesh()
        MeshRelayService.stopService(app)
    }

    fun setCategoryFilter(category: TicketCategory?) {
        _categoryFilter.value = category
    }

    fun setUrgencyFilter(urgency: UrgencyLevel?) {
        _urgencyFilter.value = urgency
    }

    fun setStatusFilter(status: TicketStatus?) {
        _statusFilter.value = status
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation(onLocationReceived: ((Location?) -> Unit)? = null) {
        viewModelScope.launch {
            _isLocating.value = true
            try {
                val cts = CancellationTokenSource()
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                    .addOnSuccessListener { location ->
                        _isLocating.value = false
                        if (location != null) {
                            _lastKnownLocation.value = location
                            onLocationReceived?.invoke(location)
                        } else {
                            // Fallback to last known location
                            fusedLocationClient.lastLocation.addOnSuccessListener { fallback ->
                                _lastKnownLocation.value = fallback
                                onLocationReceived?.invoke(fallback)
                            }
                        }
                    }
                    .addOnFailureListener {
                        _isLocating.value = false
                        onLocationReceived?.invoke(null)
                    }
            } catch (e: Exception) {
                _isLocating.value = false
                onLocationReceived?.invoke(null)
            }
        }
    }

    fun dispatchEmergencyTicket(
        category: TicketCategory,
        urgency: UrgencyLevel,
        title: String,
        description: String,
        locationDescription: String,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        viewModelScope.launch {
            val lat = latitude ?: _lastKnownLocation.value?.latitude
            val lng = longitude ?: _lastKnownLocation.value?.longitude
            syncEngine.createAndBroadcastTicket(
                category = category,
                urgency = urgency,
                title = title,
                description = description,
                latitude = lat,
                longitude = lng,
                locationDescription = locationDescription
            )
        }
    }

    fun markTicketResolved(ticketId: String) {
        viewModelScope.launch {
            syncEngine.updateStatusAndBroadcast(ticketId, TicketStatus.RESOLVED)
        }
    }

    fun markTicketAcknowledged(ticketId: String) {
        viewModelScope.launch {
            syncEngine.updateStatusAndBroadcast(ticketId, TicketStatus.ACKNOWLEDGED)
        }
    }

    fun pingPeerHandshake(endpointId: String) {
        viewModelScope.launch {
            syncEngine.initiateHandshakeWithPeer(endpointId)
        }
    }

    fun silenceAlarm() {
        alarmManager.silenceAlarm()
    }

    fun testEmergencyAlarm() {
        val testTicket = EmergencyTicket(
            ticketId = "test-alarm-${System.currentTimeMillis()}",
            authorNodeId = "peer-drill",
            category = TicketCategory.MEDICAL,
            urgency = UrgencyLevel.CRITICAL,
            title = "SIREN DRILL: Immediate Evacuation Needed",
            description = "This is a broadcast siren test to verify that device speaker and vibration alarms trigger properly during critical alerts.",
            locationDescription = "Local Perimeter Drill",
            status = TicketStatus.OPEN,
            lamportClock = syncEngine.incrementClock(),
            createdAtEpochMs = System.currentTimeMillis()
        )
        alarmManager.triggerEmergencyAlarm(testTicket)
    }
}
