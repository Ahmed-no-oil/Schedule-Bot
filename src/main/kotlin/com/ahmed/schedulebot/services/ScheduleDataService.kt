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

    fun findWeekData(weekNumber: Int): MutableList<ScheduleEntry>? {
        val week = weekRepo.findByWeekNumber(weekNumber) ?: return null
        val weekData = scheduleRepo.findByWeek(week)
        if (weekData.isNullOrEmpty()) return null
        //delete week's data if it's from last year
        if (weekData.first().dateTime.year != LocalDateTime.now().year) {
            deleteWeekData(weekData)
            return null
        } else {
            weekData.sortBy { it.day.name.value }
            return weekData
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun saveWeekData(weekOfYear: Int, data: MutableCollection<ScheduleEntry>) {
        dayRepo.findById(7).getOrElse { populateDaysTable() }
        val week = weekRepo.findByWeekNumber(weekOfYear) ?: weekRepo.save(Week(weekOfYear))
        for (i in 0..6) {
            data.elementAt(i).week = week
            scheduleRepo.save(data.elementAt(i))
        }
    }

    fun deleteScheduleEntry(entry: ScheduleEntry){
        if(scheduleRepo.existsById(entry.id))
            scheduleRepo.delete(entry)
    }

    private fun deleteWeekData(data: MutableCollection<ScheduleEntry>) {
        scheduleRepo.deleteAll(data)
    }

    private fun populateDaysTable() {
        DayOfWeek.values().forEach {
            dayRepo.save(Day(it.value, it))
        }
    }
}