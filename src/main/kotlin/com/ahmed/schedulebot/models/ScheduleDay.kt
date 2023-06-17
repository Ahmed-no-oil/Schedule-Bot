package com.ahmed.schedulebot.models

import com.ahmed.schedulebot.entities.ScheduleEntry
import java.time.OffsetDateTime

class ScheduleDay {
    var day: Int = 0
    var timeComment: String = ""
    var dateTime: OffsetDateTime = OffsetDateTime.MIN
    var isGoingToStream: Boolean = false
    var comment: String = ""

    fun of(scheduleEntry: ScheduleEntry): ScheduleDay {
        day = scheduleEntry.day.name.value
        timeComment = scheduleEntry.timeComment
        dateTime = scheduleEntry.dateTime
        isGoingToStream = scheduleEntry.isGoingToStream
        comment = scheduleEntry.comment
        return this
    }
}
