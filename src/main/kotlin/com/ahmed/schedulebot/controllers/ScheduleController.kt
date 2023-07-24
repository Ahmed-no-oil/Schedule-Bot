package com.ahmed.schedulebot.controllers

import com.ahmed.schedulebot.services.ImageService
import com.ahmed.schedulebot.models.ScheduleDayUtc
import com.ahmed.schedulebot.services.ScheduleDataService
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.util.*


@RestController
@CrossOrigin(origins = ["http://localhost:3000/", "https://elina.chat"])
class ScheduleController(val dataService: ScheduleDataService,
                         val imageService: ImageService
) {
    private val LOGGER = LoggerFactory.getLogger(ScheduleController::class.java)

    //http://localhost:8080/{year}/week-{week}/get_times_utc
    @GetMapping("/{year}/week-{week}/get_times_utc")
    fun getScheduleTimesUtc(@PathVariable year: Int, @PathVariable week: Int): ResponseEntity<List<ScheduleDayUtc>> {
        try {
            val responseData =
                    dataService.findWeekData(year,week)?.map { ScheduleDayUtc().of(it) }
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
        val stream = response.outputStream
        stream.write(imageService.getImage(year, week))
        stream.flush()
    }

    @GetMapping("/clear-cache")
    fun clearImageCache():ResponseEntity<String> {
        imageService.clearImageCache()
        return ResponseEntity.ok("Cache cleared!")
    }
}
