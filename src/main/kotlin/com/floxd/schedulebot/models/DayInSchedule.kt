package com.floxd.schedulebot.models

import java.sql.Time
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.TextStyle
import java.time.temporal.ChronoField
import java.util.*


class DayInSchedule(
    val dateTime: LocalDateTime,
    val isGoingToStream: Boolean,
    val description: String
) {
    fun dayToString():String{
        return dateTime.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    }

    fun timeToString():String{
        val formatter = DateTimeFormatter.ofPattern("H XM")
        return dateTime.format(formatter)
    }
}