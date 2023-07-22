package com.ahmed.schedulebot.controllers

import com.ahmed.schedulebot.models.ScheduleDay
import com.ahmed.schedulebot.services.ScheduleDataService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*


@RestController
@RequestMapping("/api/schedule")
@CrossOrigin(origins = ["http://localhost:3000/","https://elina.chat"])
class ScheduleController(val dataService: ScheduleDataService) {
    private val LOGGER = LoggerFactory.getLogger(ScheduleController::class.java)

    // http://localhost:8080/api/schedule/get_this_week_data
    @GetMapping("/get_this_week_data")
    fun getThisWeekData(): ResponseEntity<List<ScheduleDay>> {
        val weekNumber: Int
        val year: Int
        try {
            Calendar.getInstance().let {
                weekNumber = it[Calendar.WEEK_OF_YEAR]
                it[Calendar.DAY_OF_WEEK] = 5
                year = it[Calendar.YEAR]
            }
            val responseData =
                    dataService.findWeekData(year,weekNumber)?.map { ScheduleDay().of(it) }
                            ?: return ResponseEntity.notFound()
                                    .build()
            return ResponseEntity.ok(responseData)
        } catch (e: Exception) {
            LOGGER.error("Controller error.", e)
            return ResponseEntity.internalServerError().header("error_msg", "Couldn't get week data").build()
        }
    }

}
