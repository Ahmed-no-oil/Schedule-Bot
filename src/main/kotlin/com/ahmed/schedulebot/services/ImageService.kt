import com.ahmed.schedulebot.entities.Week
import com.ahmed.schedulebot.repositories.ScheduleEntryRepository
import com.ahmed.schedulebot.repositories.WeekRepository
import com.ahmed.schedulebot.services.ScheduleImageBuilder
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

    @Cacheable(value = ["schedule-image"], unless = "#result.size == 0")
    fun getImage(year: Int, week: Int): ByteArray {
        val weekObject = weekRepository.findByWeekNumber(week) ?: weekRepository.save(Week(week))
        val scheduleEntries = scheduleEntryRepository.findByWeek(weekObject) ?: mutableListOf()
        val imageStream = scheduleImageBuilder.create(scheduleEntries)
                .drawBackground()
                .drawWeekDates(getWeekDates())
                .drawBubbles()
                .writeDaysNames()
                .writeStreamOrNot()
                .writeTimes()
                .writeComments()
                .writeFootnote()
                .build()

        return imageStream.readAllBytes() ?: byteArrayOf()
    }

    @CacheEvict(value = ["schedule-image"], allEntries = true)
    fun clearImageCache() {
        LOGGER.info("cleared image cache")
    }

    private fun getWeekDates(): String {
        var result: String
        Calendar.getInstance().let {
            it[Calendar.DAY_OF_WEEK] = it.firstDayOfWeek
            result = it[Calendar.DAY_OF_MONTH].toString()
            it.add(Calendar.DAY_OF_WEEK, 6)
            result += "-" + SimpleDateFormat("dd MMM", Locale.ENGLISH).format(it.time)
        }
        return result
    }
}

