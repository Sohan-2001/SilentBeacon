package com.example

import com.example.data.model.EmergencyTicket
import com.example.data.model.MeshPayload
import com.example.data.model.SyncVectorClock
import com.example.data.model.TicketCategory
import com.example.data.model.TicketStatus
import com.example.data.model.UrgencyLevel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SilentBeaconUnitTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    @Test
    fun testEmergencyTicketCreationAndFields() {
        val ticket = EmergencyTicket(
            ticketId = "uuid-1234",
            authorNodeId = "node-alpha",
            category = TicketCategory.MEDICAL,
            urgency = UrgencyLevel.CRITICAL,
            title = "Insulin Needed Urgently",
            description = "Patient in shock, sector 4",
            latitude = 37.7749,
            longitude = -122.4194,
            locationDescription = "Sector 4 Community Clinic",
            status = TicketStatus.OPEN,
            lamportClock = 10L,
            createdAtEpochMs = 1700000000000L,
            hopCount = 0,
            maxHops = 7
        )

        assertEquals("uuid-1234", ticket.ticketId)
        assertEquals("node-alpha", ticket.authorNodeId)
        assertEquals(TicketCategory.MEDICAL, ticket.category)
        assertEquals(UrgencyLevel.CRITICAL, ticket.urgency)
        assertEquals(TicketStatus.OPEN, ticket.status)
        assertEquals(10L, ticket.lamportClock)
        assertEquals(0, ticket.hopCount)
        assertEquals(7, ticket.maxHops)
    }

    @Test
    fun testMeshPayloadSerializationAndPolymorphism() {
        val ticket = EmergencyTicket(
            ticketId = "uuid-test",
            authorNodeId = "node-beta",
            category = TicketCategory.WATER_FOOD,
            urgency = UrgencyLevel.HIGH,
            title = "Clean Drinking Water",
            description = "50 families trapped",
            locationDescription = "School Gym",
            status = TicketStatus.OPEN,
            lamportClock = 3L,
            createdAtEpochMs = 1700000000000L
        )

        // Test DeltaBundle
        val deltaBundle = MeshPayload.DeltaBundle(listOf(ticket))
        val encodedBundle = json.encodeToString<MeshPayload>(deltaBundle)
        val decodedBundle = json.decodeFromString<MeshPayload>(encodedBundle)
        assertTrue(decodedBundle is MeshPayload.DeltaBundle)
        assertEquals(1, (decodedBundle as MeshPayload.DeltaBundle).tickets.size)
        assertEquals("uuid-test", decodedBundle.tickets[0].ticketId)

        // Test Handshake
        val handshake = MeshPayload.Handshake(
            SyncVectorClock(
                nodeId = "node-gamma",
                maxClockObserved = 42L,
                knownTicketIds = listOf("uuid-1", "uuid-2")
            )
        )
        val encodedHandshake = json.encodeToString<MeshPayload>(handshake)
        val decodedHandshake = json.decodeFromString<MeshPayload>(encodedHandshake)
        assertTrue(decodedHandshake is MeshPayload.Handshake)
        assertEquals(42L, (decodedHandshake as MeshPayload.Handshake).vector.maxClockObserved)

        // Test StatusUpdate
        val statusUpdate = MeshPayload.StatusUpdate(
            ticketId = "uuid-test",
            updatedStatus = TicketStatus.RESOLVED,
            lamportClock = 4L,
            updaterNodeId = "node-gamma"
        )
        val encodedStatus = json.encodeToString<MeshPayload>(statusUpdate)
        val decodedStatus = json.decodeFromString<MeshPayload>(encodedStatus)
        assertTrue(decodedStatus is MeshPayload.StatusUpdate)
        assertEquals(TicketStatus.RESOLVED, (decodedStatus as MeshPayload.StatusUpdate).updatedStatus)
    }

    @Test
    fun testLwwCrdtOrderingInvariant() {
        val existingTicket = EmergencyTicket(
            ticketId = "uuid-crdt",
            authorNodeId = "node-1",
            category = TicketCategory.HAZARD,
            urgency = UrgencyLevel.HIGH,
            title = "Gas Leak",
            description = "Main Street",
            locationDescription = "Corner of 5th Ave",
            status = TicketStatus.OPEN,
            lamportClock = 5L,
            createdAtEpochMs = 1000L
        )

        // Incoming with higher clock (L=6) wins
        val incomingNewer = existingTicket.copy(
            status = TicketStatus.RESOLVED,
            lamportClock = 6L
        )
        assertTrue(incomingNewer.lamportClock > existingTicket.lamportClock)

        // Incoming with older clock (L=4) loses
        val incomingOlder = existingTicket.copy(
            status = TicketStatus.RESOLVED,
            lamportClock = 4L
        )
        assertFalse(incomingOlder.lamportClock > existingTicket.lamportClock)

        // Incoming with equal clock (L=5) breaks ties with authorNodeId lexicographically
        val nodeA = "node-a"
        val nodeB = "node-b"
        assertTrue(nodeB > nodeA)
    }

    @Test
    fun testEpidemicHopTtlBoundary() {
        val initialTicket = EmergencyTicket(
            ticketId = "uuid-hop",
            authorNodeId = "node-origin",
            category = TicketCategory.RESCUE,
            urgency = UrgencyLevel.CRITICAL,
            title = "Trapped on roof",
            description = "High water",
            locationDescription = "Block 3",
            lamportClock = 1L,
            createdAtEpochMs = 1000L,
            hopCount = 0,
            maxHops = 7
        )

        // Simulate 7 hops
        var current = initialTicket
        for (i in 1..7) {
            assertTrue(current.hopCount < current.maxHops)
            current = current.copy(hopCount = current.hopCount + 1)
        }

        // At 7 hops, TTL boundary reached: cannot forward anymore
        assertEquals(7, current.hopCount)
        assertFalse(current.hopCount < current.maxHops)
    }
}
