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

    lateinit var meshManager: NearbyConnectionsMeshManager
        private set

    lateinit var syncEngine: MeshSyncEngine
        private set

    lateinit var localNodeId: String
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = BeaconDatabase.getInstance(this)
        localNodeId = getOrCreateNodeId()

        // Create MeshManager first with late delegation to syncEngine
        meshManager = NearbyConnectionsMeshManager(
            context = this,
            localNodeId = localNodeId,
            onPayloadReceivedListener = { payload, endpointId ->
                syncEngine.handleIncomingPayload(payload, endpointId)
            },
            onPeerConnectedListener = { endpointId ->
                // Auto-trigger gossip handshake upon peer connection
                syncEngine.initiateHandshakeWithPeer(endpointId)
            }
        )

        syncEngine = MeshSyncEngine(
            dao = database.ticketDao(),
            meshManager = meshManager,
            localNodeId = localNodeId
        )
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
