package com.ahmed.schedulebot.entities

import jakarta.persistence.*
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset

@Entity
@Table(name = "history_entries")
class HistoryEntry() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private var id: Int = 0
    var timestamp :LocalDateTime = LocalDateTime.MIN
    var userName: String = ""
    var interaction: String = ""

    constructor(interaction: String, userName: String): this(){
        timestamp = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime()
        this.userName= userName
        this.interaction= interaction
    }

    override fun toString(): String {
        return "<t:${timestamp.toEpochSecond(ZoneOffset.UTC)}:f>: $userName $interaction"
    }
}