package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.EmergencyTicket
import com.example.data.model.TicketStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {
    @Query("SELECT * FROM tickets ORDER BY CASE urgency WHEN 'CRITICAL' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'MEDIUM' THEN 3 WHEN 'LOW' THEN 4 ELSE 5 END ASC, lamportClock DESC")
    fun observeAllTickets(): Flow<List<EmergencyTicket>>

    @Query("SELECT * FROM tickets WHERE ticketId = :id")
    suspend fun getTicketById(id: String): EmergencyTicket?

    @Query("SELECT ticketId FROM tickets")
    suspend fun getAllTicketIds(): List<String>

    @Query("SELECT MAX(lamportClock) FROM tickets")
    suspend fun getMaxClock(): Long?

    @Query("SELECT COUNT(*) FROM tickets")
    suspend fun getTicketCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(ticket: EmergencyTicket)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(tickets: List<EmergencyTicket>)

    @Query("UPDATE tickets SET status = :status, lamportClock = :newClock WHERE ticketId = :ticketId AND lamportClock < :newClock")
    suspend fun updateStatusIfNewer(ticketId: String, status: TicketStatus, newClock: Long): Int

    @Query("DELETE FROM tickets WHERE ticketId = :ticketId")
    suspend fun deleteTicket(ticketId: String)
}
