package com.floxd.schedulebot.models

import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*


class DayInSchedule() {
    lateinit var day: DayOfWeek
    var timeComment: String = ""
    var timeData: LocalTime = LocalTime.MIN
    var isGoingToStream: Boolean = true
    var comment: String = ""
    var authorName: String = ""

    override fun toString(): String{
        val s = if(isGoingToStream) "Stream" else "No Stream"
        return "${day.name}: $s $timeComment $comment -by $authorName"
    }
}