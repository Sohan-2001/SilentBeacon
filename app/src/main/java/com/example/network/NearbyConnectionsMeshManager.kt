package com.example.network

import android.content.Context
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.example.data.model.MeshActivityLog
import com.example.data.model.MeshPayload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.charset.StandardCharsets

class NearbyConnectionsMeshManager(
    private val context: Context,
    val localNodeId: String,
    private val onPayloadReceivedListener: suspend (MeshPayload, String) -> Unit,
    private val onPeerConnectedListener: (suspend (String) -> Unit)? = null
) {
    companion object {
        private const val TAG = "NearbyMeshManager"
        const val SERVICE_ID = "com.silentbeacon.mesh.v1"
    }

    private val client = Nearby.getConnectionsClient(context)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val _connectedPeers = MutableStateFlow<Set<String>>(emptySet())
    val connectedPeers: StateFlow<Set<String>> = _connectedPeers.asStateFlow()

    private val _isMeshActive = MutableStateFlow(false)
    val isMeshActive: StateFlow<Boolean> = _isMeshActive.asStateFlow()

    private val _activityLogs = MutableSharedFlow<MeshActivityLog>(replay = 50)
    val activityLogs: SharedFlow<MeshActivityLog> = _activityLogs.asSharedFlow()

    private val connectingEndpoints = mutableSetOf<String>()

    fun startMesh() {
        if (_isMeshActive.value) return
        _isMeshActive.value = true
        logActivity("RADIO", "Starting BLE & Wi-Fi Direct Mesh (P2P Cluster)")
        startAdvertising()
        startDiscovery()
    }

    fun stopMesh() {
        if (!_isMeshActive.value) return
        _isMeshActive.value = false
        logActivity("RADIO", "Stopping Radio Mesh & Disconnecting peers")
        try {
            client.stopAdvertising()
            client.stopDiscovery()
            client.stopAllEndpoints()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping Nearby", e)
        }
        connectingEndpoints.clear()
        _connectedPeers.value = emptySet()
    }

    private fun startAdvertising() {
        val advertisingOptions = AdvertisingOptions.Builder()
            .setStrategy(Strategy.P2P_CLUSTER)
            .build()

        client.startAdvertising(localNodeId, SERVICE_ID, connectionLifecycleCallback, advertisingOptions)
            .addOnSuccessListener {
                logActivity("BLE_ADV", "Advertising broadcast beacon: $localNodeId")
            }
            .addOnFailureListener { e ->
                logActivity("BLE_ADV_ERR", "Advertising error: ${e.localizedMessage}")
                Log.e(TAG, "Failed to start advertising", e)
            }
    }

    private fun startDiscovery() {
        val discoveryOptions = DiscoveryOptions.Builder()
            .setStrategy(Strategy.P2P_CLUSTER)
            .build()

        client.startDiscovery(SERVICE_ID, endpointDiscoveryCallback, discoveryOptions)
            .addOnSuccessListener {
                logActivity("BLE_DISC", "Scanning for nearby emergency radio beacons...")
            }
            .addOnFailureListener { e ->
                logActivity("BLE_DISC_ERR", "Discovery error: ${e.localizedMessage}")
                Log.e(TAG, "Failed to start discovery", e)
            }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            logActivity("PEER_FOUND", "Discovered peer: ${info.endpointName} ($endpointId)")
            if (endpointId !in _connectedPeers.value && endpointId !in connectingEndpoints) {
                connectingEndpoints.add(endpointId)
                logActivity("CONNECTING", "Initiating peer link with $endpointId")
                client.requestConnection(localNodeId, endpointId, connectionLifecycleCallback)
                    .addOnFailureListener { e ->
                        connectingEndpoints.remove(endpointId)
                        logActivity("CONN_FAIL", "Link rejected by $endpointId: ${e.message}")
                    }
            }
        }

        override fun onEndpointLost(endpointId: String) {
            logActivity("PEER_LOST", "Radio link lost with $endpointId")
            connectingEndpoints.remove(endpointId)
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            logActivity("HANDSHAKE", "Unattended pairing with ${connectionInfo.endpointName} ($endpointId)")
            client.acceptConnection(endpointId, payloadCallback)
                .addOnFailureListener { e ->
                    logActivity("ACCEPT_FAIL", "Failed accepting connection from $endpointId: ${e.message}")
                }
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            connectingEndpoints.remove(endpointId)
            if (result.status.isSuccess) {
                logActivity("LINK_ESTABLISHED", "Peer online: $endpointId")
                _connectedPeers.value = _connectedPeers.value + endpointId
                scope.launch {
                    try {
                        onPeerConnectedListener?.invoke(endpointId)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in onPeerConnectedListener", e)
                    }
                }
            } else {
                logActivity("LINK_REFUSED", "Connection failed ($endpointId): ${result.status.statusMessage}")
                _connectedPeers.value = _connectedPeers.value - endpointId
            }
        }

        override fun onDisconnected(endpointId: String) {
            logActivity("DISCONNECTED", "Disconnected from $endpointId")
            connectingEndpoints.remove(endpointId)
            _connectedPeers.value = _connectedPeers.value - endpointId
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                payload.asBytes()?.let { bytes ->
                    val jsonStr = String(bytes, StandardCharsets.UTF_8)
                    scope.launch {
                        try {
                            val decoded = json.decodeFromString<MeshPayload>(jsonStr)
                            val typeLabel = when (decoded) {
                                is MeshPayload.Handshake -> "Handshake (${decoded.vector.knownTicketIds.size} tickets, clock ${decoded.vector.maxClockObserved})"
                                is MeshPayload.DeltaRequest -> "DeltaRequest (${decoded.missingTicketIds.size} missing)"
                                is MeshPayload.DeltaBundle -> "DeltaBundle (${decoded.tickets.size} tickets)"
                                is MeshPayload.StatusUpdate -> "StatusUpdate (${decoded.ticketId.take(8)} -> ${decoded.updatedStatus})"
                            }
                            logActivity("RX_PACKET", "From $endpointId: $typeLabel", isIncoming = true)
                            onPayloadReceivedListener(decoded, endpointId)
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to decode incoming mesh packet: ${e.message}")
                        }
                    }
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }

    suspend fun sendPayload(endpointId: String, payload: MeshPayload) = withContext(Dispatchers.IO) {
        try {
            val jsonStr = json.encodeToString(payload)
            val bytes = jsonStr.toByteArray(StandardCharsets.UTF_8)
            client.sendPayload(endpointId, Payload.fromBytes(bytes))
            logActivity("TX_PACKET", "Sent to $endpointId: ${payload::class.simpleName}", isIncoming = false)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending payload to $endpointId", e)
        }
    }

    suspend fun broadcastPayload(payload: MeshPayload) = withContext(Dispatchers.IO) {
        val targets = _connectedPeers.value.toList()
        if (targets.isNotEmpty()) {
            try {
                val jsonStr = json.encodeToString(payload)
                val bytes = jsonStr.toByteArray(StandardCharsets.UTF_8)
                client.sendPayload(targets, Payload.fromBytes(bytes))
                logActivity("TX_BROADCAST", "Broadcast to ${targets.size} peers: ${payload::class.simpleName}", isIncoming = false)
            } catch (e: Exception) {
                Log.e(TAG, "Error broadcasting payload", e)
            }
        }
    }

    fun logActivity(tag: String, message: String, isIncoming: Boolean? = null) {
        scope.launch {
            _activityLogs.emit(MeshActivityLog(tag = tag, message = message, isIncoming = isIncoming))
        }
    }
}
