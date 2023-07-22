package com.ahmed.schedulebot.services

import com.ahmed.schedulebot.entities.Day
import com.ahmed.schedulebot.entities.ScheduleEntry
import com.ahmed.schedulebot.entities.Week
import com.ahmed.schedulebot.repositories.DayRepository
import com.ahmed.schedulebot.repositories.ScheduleEntryRepository
import com.ahmed.schedulebot.repositories.WeekRepository
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrElse

@Service
class ScheduleDataService(
    val scheduleRepo: ScheduleEntryRepository,
    val dayRepo: DayRepository,
    val weekRepo: WeekRepository
) {

    fun findWeekData(year:Int, weekNumber: Int): MutableList<ScheduleEntry>? {
        val week = weekRepo.findByYearAndWeekNumber(year,weekNumber) ?: return null
        val weekData = scheduleRepo.findByWeek(week) ?: return null
        weekData.sortBy { it.day.name.value }
        return weekData
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun saveWeekData(year:Int ,weekOfYear: Int, data: MutableCollection<ScheduleEntry>) {
        dayRepo.findById(7).getOrElse { populateDaysTable() }
        val week = weekRepo.findByYearAndWeekNumber(year,weekOfYear) ?: weekRepo.save(Week(year, weekOfYear))
        for (i in 0..6) {
            data.elementAt(i).week = week
            scheduleRepo.save(data.elementAt(i))
        }
    }

    fun deleteScheduleEntry(entry: ScheduleEntry) {
        if (scheduleRepo.existsById(entry.id))
            scheduleRepo.delete(entry)
    }

    private fun populateDaysTable() {
        DayOfWeek.values().forEach {
            dayRepo.save(Day(it.value, it))
        }
    }
}
