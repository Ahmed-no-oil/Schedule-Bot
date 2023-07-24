package com.ahmed.schedulebot.models

import com.ahmed.schedulebot.entities.ScheduleEntry
import java.time.OffsetDateTime

class ScheduleDayUtc {
    var id: Int = 0
    var isGoingToStream: Boolean = false
    var dateTime: OffsetDateTime = OffsetDateTime.MIN

    fun of(scheduleEntry: ScheduleEntry): ScheduleDayUtc {
        id = scheduleEntry.day.name.value
        dateTime = scheduleEntry.dateTime
        isGoingToStream = scheduleEntry.isGoingToStream
        return this
    }
}