package com.ahmed.schedulebot.models

import java.time.DayOfWeek
import java.time.LocalTime


class DayInSchedule() {
    var day: DayOfWeek = DayOfWeek.MONDAY
    var timeComment: String = ""
    var timeData: LocalTime = LocalTime.MIN
    var isGoingToStream: Boolean = false
    var comment: String = ""
    var authorName: String = ""

    constructor(dayOfWeek: DayOfWeek) : this() {
        day = dayOfWeek
    }

    override fun toString(): String {
        val s = if (isGoingToStream) "Stream" else "No Stream"
        return "${day.name}: $s $timeComment $comment -by $authorName"
    }
}
