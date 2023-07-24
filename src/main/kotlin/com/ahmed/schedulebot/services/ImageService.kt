package com.ahmed.schedulebot.services

import com.ahmed.schedulebot.entities.Week
import com.ahmed.schedulebot.repositories.ScheduleEntryRepository
import com.ahmed.schedulebot.repositories.WeekRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat
import java.util.*

@Service
class ImageService(val scheduleEntryRepository: ScheduleEntryRepository,
                   val weekRepository: WeekRepository,
                   val scheduleImageBuilder: ScheduleImageBuilder) {

    private val LOGGER: Logger = LoggerFactory.getLogger(ImageService::class.java)

    @Cacheable(value = ["schedule-image"], unless = "#result.length == 0")
    fun getImage(year: Int, week: Int): ByteArray {
        val weekObject = weekRepository.findByYearAndWeekNumber(year, week) ?: weekRepository.save(Week(year, week))
        val scheduleEntries = scheduleEntryRepository.findByWeek(weekObject) ?: mutableListOf()
        val imageStream = scheduleImageBuilder.create(scheduleEntries)
            .drawBackground()
            .drawWeekDates(getWeekDates(year, week))
            .drawBubbles()
            .writeDaysNames()
            .writeStreamOrNot()
            .writeTimes()
            .writeComments()
            .build()

        return imageStream.readAllBytes() ?: byteArrayOf()
    }

    @CacheEvict(value = ["schedule-image"], allEntries = true)
    fun clearImageCache() {
        LOGGER.info("cleared image cache")
    }

    private fun getWeekDates(year: Int, week: Int): String {
        var result: String
        Calendar.getInstance().let {
            it.firstDayOfWeek = Calendar.MONDAY
            it[Calendar.YEAR] = year
            it[Calendar.WEEK_OF_YEAR] = week
            it[Calendar.DAY_OF_WEEK] = it.firstDayOfWeek
            result = it[Calendar.DAY_OF_MONTH].toString()
            it.add(Calendar.DAY_OF_WEEK, 6)
            result += "-" + SimpleDateFormat("dd MMM", Locale.ENGLISH).format(it.time)
        }
        return result
    }
}

