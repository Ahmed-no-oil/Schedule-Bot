package com.ahmed.schedulebot.controllers

import ImageService
import com.ahmed.schedulebot.models.ScheduleDay
import com.ahmed.schedulebot.services.ScheduleDataService
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.*


@RestController
@CrossOrigin(origins = ["http://localhost:3000/", "https://elina.chat"])
class ScheduleController(val dataService: ScheduleDataService,
                         val imageService: ImageService) {
    private val LOGGER = LoggerFactory.getLogger(ScheduleController::class.java)

    // http://localhost:8080/api/schedule/get_this_week_data
    @GetMapping("/api/schedule/get_this_week_data")
    fun getThisWeekData(): ResponseEntity<List<ScheduleDay>> {
        val weekNumber: Int
        try {
            Calendar.getInstance().let {
                weekNumber = it[Calendar.WEEK_OF_YEAR]
            }
            val responseData =
                    dataService.findWeekData(weekNumber)?.map { ScheduleDay().of(it) }
                            ?: return ResponseEntity.notFound()
                                    .build()
            return ResponseEntity.ok(responseData)
        } catch (e: Exception) {
            LOGGER.error("Controller error.", e)
            return ResponseEntity.internalServerError().header("error_msg", "Couldn't get week data").build()
        }
    }

    @GetMapping("/{year}/week-{week}/schedule.jpg")
    fun getScheduleImage(@PathVariable year: Int, @PathVariable week: Int, response: HttpServletResponse) {
        val stream = response.getOutputStream()
        stream.write(imageService.getImage(year, week))
        stream.flush()
    }

    @GetMapping("/clear-cache")
    fun clearImageCache() {
        imageService.clearImageCache()
    }
}
