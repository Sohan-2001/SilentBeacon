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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import com.example.network.MeshRadioStatus

data class DashboardUiState(
    val tickets: List<EmergencyTicket> = emptyList(),
    val filteredTickets: List<EmergencyTicket> = emptyList(),
    val connectedPeers: Set<String> = emptySet(),
    val isMeshActive: Boolean = false,
    val radioStatus: MeshRadioStatus = MeshRadioStatus.STOPPED,
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
    private val alarmManager = app.alarmManager
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    private val _categoryFilter = MutableStateFlow<TicketCategory?>(null)
    private val _urgencyFilter = MutableStateFlow<UrgencyLevel?>(null)
    private val _statusFilter = MutableStateFlow<TicketStatus?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _lastKnownLocation = MutableStateFlow<Location?>(null)
    private val _isLocating = MutableStateFlow(false)
    private val _activityLogs = MutableStateFlow<List<MeshActivityLog>>(emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val peersFlow: Flow<Set<String>> = app.meshManagerFlow.flatMapLatest { it?.connectedPeers ?: flowOf(emptySet()) }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val meshActiveFlow: Flow<Boolean> = app.meshManagerFlow.flatMapLatest { it?.isMeshActive ?: flowOf(false) }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val radioStatusFlow: Flow<MeshRadioStatus> = app.meshManagerFlow.flatMapLatest { it?.radioStatus ?: flowOf(MeshRadioStatus.STOPPED) }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val relaysFlow: Flow<Int> = app.syncEngineFlow.flatMapLatest { it?.relayedPacketsCount ?: flowOf(0) }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val syncedFlow: Flow<Int> = app.syncEngineFlow.flatMapLatest { it?.syncedTicketsCount ?: flowOf(0) }

    val uiState: StateFlow<DashboardUiState> = combine(
        dao.observeAllTickets(),
        peersFlow,
        meshActiveFlow,
        radioStatusFlow,
        _categoryFilter,
        _urgencyFilter,
        _statusFilter,
        _searchQuery,
        relaysFlow,
        syncedFlow,
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
        val radioStatus = params[3] as MeshRadioStatus
        val catFilter = params[4] as TicketCategory?
        val urgFilter = params[5] as UrgencyLevel?
        val statusFilter = params[6] as TicketStatus?
        val query = params[7] as String
        val relays = params[8] as Int
        val synced = params[9] as Int
        val loc = params[10] as Location?
        val isLocating = params[11] as Boolean
        @Suppress("UNCHECKED_CAST")
        val logs = params[12] as List<MeshActivityLog>
        val isAlarmRinging = params[13] as Boolean
        val activeAlarmTicket = params[14] as EmergencyTicket?

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
            radioStatus = radioStatus,
            selectedCategoryFilter = catFilter,
            selectedUrgencyFilter = urgFilter,
            selectedStatusFilter = statusFilter,
            searchQuery = query,
            localNodeId = app.localNodeId,
            lamportClock = app.syncEngine?.getClock() ?: 0L,
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
        // Collect radio activity logs dynamically whenever mesh manager is active
        viewModelScope.launch {
            app.meshManagerFlow.collectLatest { manager ->
                manager?.activityLogs?.collect { newLog ->
                    _activityLogs.value = (listOf(newLog) + _activityLogs.value).take(100)
                }
            }
        }
        // Fetch initial GPS
        fetchCurrentLocation()
    }

    fun startMeshService() {
        if (app.meshManager == null) {
            app.initMeshLayerSafely()
        }
        MeshRelayService.startService(app)
        app.meshManager?.startMesh()
    }

    fun simulateNearbyPeer(): String? {
        return app.meshManager?.simulatePeerConnection()
    }

    fun disconnectSimulatedPeer(peerId: String) {
        app.meshManager?.simulatePeerDisconnection(peerId)
    }

    fun stopMeshService() {
        app.meshManager?.stopMesh()
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
            val engine = app.syncEngine
            if (engine != null) {
                engine.createAndBroadcastTicket(
                    category = category,
                    urgency = urgency,
                    title = title,
                    description = description,
                    latitude = lat,
                    longitude = lng,
                    locationDescription = locationDescription
                )
            } else {
                // If radios are disabled or unsupported, safely save to local Room database
                val fallbackTicket = EmergencyTicket(
                    ticketId = UUID.randomUUID().toString(),
                    authorNodeId = app.localNodeId,
                    category = category,
                    urgency = urgency,
                    title = title,
                    description = description,
                    latitude = lat,
                    longitude = lng,
                    locationDescription = locationDescription,
                    status = TicketStatus.OPEN,
                    lamportClock = 1L,
                    createdAtEpochMs = System.currentTimeMillis()
                )
                dao.insertOrUpdate(fallbackTicket)
            }
        }
    }

    fun markTicketResolved(ticketId: String) {
        viewModelScope.launch {
            val engine = app.syncEngine
            if (engine != null) {
                engine.updateStatusAndBroadcast(ticketId, TicketStatus.RESOLVED)
            } else {
                dao.getTicketById(ticketId)?.let {
                    dao.insertOrUpdate(it.copy(status = TicketStatus.RESOLVED))
                }
            }
        }
    }

    fun markTicketAcknowledged(ticketId: String) {
        viewModelScope.launch {
            val engine = app.syncEngine
            if (engine != null) {
                engine.updateStatusAndBroadcast(ticketId, TicketStatus.ACKNOWLEDGED)
            } else {
                dao.getTicketById(ticketId)?.let {
                    dao.insertOrUpdate(it.copy(status = TicketStatus.ACKNOWLEDGED))
                }
            }
        }
    }

    fun deleteTicket(ticketId: String) {
        viewModelScope.launch {
            dao.deleteTicket(ticketId)
        }
    }

    fun clearAllResolvedTickets() {
        viewModelScope.launch {
            dao.deleteAllResolvedTickets()
        }
    }

    fun pingPeerHandshake(endpointId: String) {
        viewModelScope.launch {
            app.syncEngine?.initiateHandshakeWithPeer(endpointId)
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
            lamportClock = app.syncEngine?.incrementClock() ?: 1L,
            createdAtEpochMs = System.currentTimeMillis()
        )
        alarmManager.triggerEmergencyAlarm(testTicket)
    }
}
