package com.ahmed.schedulebot.services

import com.ahmed.schedulebot.entities.HistoryEntry
import com.ahmed.schedulebot.repositories.HistoryEntryRepository
import org.springframework.stereotype.Service

@Service
class HistoryService(val historyRepo: HistoryEntryRepository) {
    fun log(interaction: String, userName: String) {
        historyRepo.save(HistoryEntry(interaction, userName))
    }

    fun getLast20Logs(): MutableList<HistoryEntry> {
        return historyRepo.findFirst20ByOrderByTimestamp()
    }
}
