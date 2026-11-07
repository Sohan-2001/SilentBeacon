package com.example.data.sync

import com.example.data.local.TicketDao
import com.example.data.model.EmergencyTicket
import com.example.data.model.MeshPayload
import com.example.data.model.SyncVectorClock
import com.example.data.model.TicketCategory
import com.example.data.model.TicketStatus
import com.example.data.model.UrgencyLevel
import com.example.network.NearbyConnectionsMeshManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

class MeshSyncEngine(
    private val dao: TicketDao,
    private val meshManager: NearbyConnectionsMeshManager,
    val localNodeId: String
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val localLamportClock = AtomicLong(0)

    private val _relayedPacketsCount = MutableStateFlow(0)
    val relayedPacketsCount: StateFlow<Int> = _relayedPacketsCount.asStateFlow()

    private val _syncedTicketsCount = MutableStateFlow(0)
    val syncedTicketsCount: StateFlow<Int> = _syncedTicketsCount.asStateFlow()

    init {
        scope.launch {
            val maxDbClock = dao.getMaxClock() ?: 0L
            localLamportClock.set(maxDbClock)
            meshManager.logActivity("SYNC_INIT", "CRDT Vector Clock initialized at L=$maxDbClock")
        }
    }

    fun getClock(): Long = localLamportClock.get()

    fun incrementClock(): Long = localLamportClock.incrementAndGet()

    fun updateClockOnReceived(remoteClock: Long): Long {
        return localLamportClock.updateAndGet { current -> maxOf(current, remoteClock) + 1 }
    }

    suspend fun handleIncomingPayload(payload: MeshPayload, fromEndpointId: String) {
        when (payload) {
            is MeshPayload.Handshake -> {
                updateClockOnReceived(payload.vector.maxClockObserved)
                val allKnownLocalIds = dao.getAllTicketIds()
                val peerKnownIds = payload.vector.knownTicketIds.toSet()

                // 1. Send tickets that local has but peer is missing
                val missingIdsForPeer = allKnownLocalIds.filter { it !in peerKnownIds }
                if (missingIdsForPeer.isNotEmpty()) {
                    val ticketsToSend = missingIdsForPeer.mapNotNull { dao.getTicketById(it) }
                    meshManager.sendPayload(fromEndpointId, MeshPayload.DeltaBundle(ticketsToSend))
                    meshManager.logActivity("SYNC_PUSH", "Pushing ${ticketsToSend.size} tickets to $fromEndpointId")
                }

                // 2. Request tickets that peer has but local is missing
                val missingIdsForLocal = peerKnownIds.filter { it !in allKnownLocalIds.toSet() }
                if (missingIdsForLocal.isNotEmpty()) {
                    meshManager.sendPayload(fromEndpointId, MeshPayload.DeltaRequest(missingIdsForLocal))
                    meshManager.logActivity("SYNC_PULL", "Requesting ${missingIdsForLocal.size} tickets from $fromEndpointId")
                }
            }

            is MeshPayload.DeltaRequest -> {
                val requestedTickets = payload.missingTicketIds.mapNotNull { dao.getTicketById(it) }
                if (requestedTickets.isNotEmpty()) {
                    meshManager.sendPayload(fromEndpointId, MeshPayload.DeltaBundle(requestedTickets))
                    meshManager.logActivity("SYNC_SERVE", "Served ${requestedTickets.size} requested tickets to $fromEndpointId")
                }
            }

            is MeshPayload.DeltaBundle -> {
                val forwardableTickets = mutableListOf<EmergencyTicket>()
                var newlyInserted = 0

                for (ticket in payload.tickets) {
                    updateClockOnReceived(ticket.lamportClock)
                    val existing = dao.getTicketById(ticket.ticketId)

                    // LWW-CRDT Merge Invariant:
                    // Higher Lamport Clock wins.
                    // On tie, lexicographical node ID order breaks ties deterministically.
                    val shouldAccept = when {
                        existing == null -> true
                        ticket.lamportClock > existing.lamportClock -> true
                        ticket.lamportClock == existing.lamportClock -> ticket.authorNodeId > existing.authorNodeId
                        else -> false
                    }

                    if (shouldAccept) {
                        dao.insertOrUpdate(ticket)
                        newlyInserted++
                        // Check TTL boundary (Epidemic Hop Count)
                        if (ticket.hopCount < ticket.maxHops) {
                            forwardableTickets.add(ticket.copy(hopCount = ticket.hopCount + 1))
                        }
                    }
                }

                if (newlyInserted > 0) {
                    _syncedTicketsCount.value += newlyInserted
                }

                // Gossip relay forwardable packets to all connected peers in cluster
                if (forwardableTickets.isNotEmpty()) {
                    _relayedPacketsCount.value += forwardableTickets.size
                    meshManager.broadcastPayload(MeshPayload.DeltaBundle(forwardableTickets))
                    meshManager.logActivity("GOSSIP_RELAY", "Relaying ${forwardableTickets.size} tickets across mesh")
                }
            }

            is MeshPayload.StatusUpdate -> {
                updateClockOnReceived(payload.lamportClock)
                val existing = dao.getTicketById(payload.ticketId)
                if (existing != null) {
                    val isNewer = when {
                        payload.lamportClock > existing.lamportClock -> true
                        payload.lamportClock == existing.lamportClock -> payload.updaterNodeId > existing.authorNodeId
                        else -> false
                    }

                    if (isNewer) {
                        val updated = existing.copy(
                            status = payload.updatedStatus,
                            lamportClock = payload.lamportClock
                        )
                        dao.insertOrUpdate(updated)
                        meshManager.logActivity("STATUS_UPDATED", "Ticket ${payload.ticketId.take(8)} -> ${payload.updatedStatus}")
                        // Forward status progression through mesh
                        meshManager.broadcastPayload(payload)
                    }
                }
            }
        }
    }

    suspend fun initiateHandshakeWithPeer(endpointId: String) {
        val knownIds = dao.getAllTicketIds()
        val maxClock = maxOf(dao.getMaxClock() ?: 0L, localLamportClock.get())
        val handshake = MeshPayload.Handshake(
            SyncVectorClock(
                nodeId = localNodeId,
                maxClockObserved = maxClock,
                knownTicketIds = knownIds
            )
        )
        meshManager.logActivity("HANDSHAKE_INIT", "Sending vector clock (L=$maxClock, ${knownIds.size} tickets) to $endpointId")
        meshManager.sendPayload(endpointId, handshake)
    }

    suspend fun createAndBroadcastTicket(
        category: TicketCategory,
        urgency: UrgencyLevel,
        title: String,
        description: String,
        latitude: Double?,
        longitude: Double?,
        locationDescription: String
    ): EmergencyTicket {
        val clock = incrementClock()
        val ticket = EmergencyTicket(
            ticketId = UUID.randomUUID().toString(),
            authorNodeId = localNodeId,
            category = category,
            urgency = urgency,
            title = title,
            description = description,
            latitude = latitude,
            longitude = longitude,
            locationDescription = locationDescription,
            status = TicketStatus.OPEN,
            lamportClock = clock,
            createdAtEpochMs = System.currentTimeMillis(),
            hopCount = 0
        )
        dao.insertOrUpdate(ticket)
        meshManager.logActivity("DISPATCH_SOS", "Broadcasting new SOS [${category.name} | ${urgency.name}] L=$clock")
        meshManager.broadcastPayload(MeshPayload.DeltaBundle(listOf(ticket)))
        return ticket
    }

    suspend fun updateStatusAndBroadcast(ticketId: String, newStatus: TicketStatus): Boolean {
        val clock = incrementClock()
        val existing = dao.getTicketById(ticketId) ?: return false
        val updated = existing.copy(status = newStatus, lamportClock = clock)
        dao.insertOrUpdate(updated)
        val payload = MeshPayload.StatusUpdate(
            ticketId = ticketId,
            updatedStatus = newStatus,
            lamportClock = clock,
            updaterNodeId = localNodeId
        )
        meshManager.logActivity("STATUS_BROADCAST", "Broadcasting status ${newStatus.name} for ${ticketId.take(8)} (L=$clock)")
        meshManager.broadcastPayload(payload)
        return true
    }
}
