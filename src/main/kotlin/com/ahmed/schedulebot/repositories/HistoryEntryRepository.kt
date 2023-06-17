package com.ahmed.schedulebot.repositories

import com.ahmed.schedulebot.entities.HistoryEntry
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface HistoryEntryRepository : CrudRepository<HistoryEntry, Int> {
    fun findFirst20ByOrderByTimestamp(): MutableList<HistoryEntry>
}
