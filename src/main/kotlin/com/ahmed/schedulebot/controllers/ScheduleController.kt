package com.ahmed.schedulebot.controllers

import com.ahmed.schedulebot.models.ScheduleDay
import com.ahmed.schedulebot.services.ScheduleDataService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*


@RestController
@RequestMapping("/api/schedule")
class ScheduleController(val dataService: ScheduleDataService) {

    // http://localhost:8080/api/schedule/get_this_week_data
    @GetMapping("/get_this_week_data")
    fun getThisWeekData(): ResponseEntity<List<ScheduleDay>> {
        val weekNumber: Int
        Calendar.getInstance().let {
            weekNumber = it[Calendar.WEEK_OF_YEAR]
        }
        val responseData = dataService.findWeekData(weekNumber)?.map { ScheduleDay().of(it) } ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(responseData)
    }
}