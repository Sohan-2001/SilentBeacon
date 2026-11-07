package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class UrgencyLevel {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW
}

enum class TicketCategory {
    MEDICAL,
    WATER_FOOD,
    RESCUE,
    SHELTER,
    HAZARD
}

enum class TicketStatus {
    OPEN,
    ACKNOWLEDGED,
    RESOLVED
}

@Serializable
@Entity(tableName = "tickets")
data class EmergencyTicket(
    @PrimaryKey val ticketId: String,          // UUID v4
    val authorNodeId: String,                  // SHA-256 slice of device install ID
    val category: TicketCategory,
    val urgency: UrgencyLevel,
    val title: String,
    val description: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationDescription: String,
    val status: TicketStatus = TicketStatus.OPEN,
    val lamportClock: Long,                    // Logical timestamp for CRDT ordering
    val createdAtEpochMs: Long,
    val hopCount: Int = 0,                     // Incremented each relay hop
    val maxHops: Int = 7                       // TTL boundary
)

@Serializable
data class SyncVectorClock(
    val nodeId: String,
    val maxClockObserved: Long,
    val knownTicketIds: List<String>
)

@Serializable
sealed class MeshPayload {
    @Serializable
    @SerialName("handshake")
    data class Handshake(val vector: SyncVectorClock) : MeshPayload()

    @Serializable
    @SerialName("delta_request")
    data class DeltaRequest(val missingTicketIds: List<String>) : MeshPayload()

    @Serializable
    @SerialName("delta_bundle")
    data class DeltaBundle(val tickets: List<EmergencyTicket>) : MeshPayload()

    @Serializable
    @SerialName("status_update")
    data class StatusUpdate(
        val ticketId: String,
        val updatedStatus: TicketStatus,
        val lamportClock: Long,
        val updaterNodeId: String
    ) : MeshPayload()
}

data class MeshActivityLog(
    val timestampMs: Long = System.currentTimeMillis(),
    val tag: String,
    val message: String,
    val isIncoming: Boolean? = null
)
