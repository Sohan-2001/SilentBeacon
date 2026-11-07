package com.example

import android.app.Application
import android.content.Context
import com.example.data.local.BeaconDatabase
import com.example.data.sync.MeshSyncEngine
import com.example.network.NearbyConnectionsMeshManager
import java.security.MessageDigest
import java.util.UUID

class SilentBeaconApp : Application() {

    lateinit var database: BeaconDatabase
        private set

    var meshManager: NearbyConnectionsMeshManager? = null
        private set

    var syncEngine: MeshSyncEngine? = null
        private set

    val meshManagerFlow = kotlinx.coroutines.flow.MutableStateFlow<NearbyConnectionsMeshManager?>(null)
    val syncEngineFlow = kotlinx.coroutines.flow.MutableStateFlow<MeshSyncEngine?>(null)

    lateinit var alarmManager: com.example.alarm.EmergencyAlarmManager
        private set

    lateinit var localNodeId: String
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = BeaconDatabase.getInstance(this)
        localNodeId = getOrCreateNodeId()
        alarmManager = com.example.alarm.EmergencyAlarmManager(this)

        // Do NOT eagerly initialize meshManager or syncEngine here!
        // On Android 10 Go devices or phones without Google Play Services,
        // Nearby.getConnectionsClient() crashes before MainActivity can even show a diagnostic screen.
        // Mesh initialization is deferred to initMeshLayerSafely() after DeviceCompatibilityChecker runs.
    }

    /**
     * Safely attempts to initialize the Nearby Connections mesh radio layer.
     * Returns true if successful, or false with clean exception logging if unsupported/missing.
     */
    @Synchronized
    fun initMeshLayerSafely(): Boolean {
        if (meshManager != null && syncEngine != null) return true

        return try {
            val manager = NearbyConnectionsMeshManager(
                context = this,
                localNodeId = localNodeId,
                onPayloadReceivedListener = { payload, endpointId ->
                    syncEngine?.handleIncomingPayload(payload, endpointId)
                },
                onPeerConnectedListener = { endpointId ->
                    syncEngine?.initiateHandshakeWithPeer(endpointId)
                }
            )

            val engine = MeshSyncEngine(
                dao = database.ticketDao(),
                meshManager = manager,
                localNodeId = localNodeId,
                onCriticalAlertReceived = { ticket ->
                    alarmManager.triggerEmergencyAlarm(ticket)
                },
                onAlertResolved = { ticketId ->
                    alarmManager.onTicketResolved(ticketId)
                }
            )

            meshManager = manager
            syncEngine = engine
            meshManagerFlow.value = manager
            syncEngineFlow.value = engine
            true
        } catch (t: Throwable) {
            android.util.Log.e("SilentBeaconApp", "Failed to safely initialize Nearby Connections mesh", t)
            false
        }
    }

    private fun getOrCreateNodeId(): String {
        val prefs = getSharedPreferences("silent_beacon_prefs", Context.MODE_PRIVATE)
        var nodeId = prefs.getString("node_id", null)
        if (nodeId == null) {
            val rawId = UUID.randomUUID().toString()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(rawId.toByteArray())
            // Take 8-character hex slice for clean UI representation & deterministic ordering
            nodeId = digest.take(4).joinToString("") { "%02x".format(it) }
            prefs.edit().putString("node_id", nodeId).apply()
        }
        return nodeId
    }

    companion object {
        lateinit var instance: SilentBeaconApp
            private set
    }
}
