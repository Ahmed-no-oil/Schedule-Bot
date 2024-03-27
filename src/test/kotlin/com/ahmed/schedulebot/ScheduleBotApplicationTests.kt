package com.ahmed.schedulebot

import com.ahmed.schedulebot.entities.Week
import com.ahmed.schedulebot.repositories.ScheduleEntryRepository
import com.ahmed.schedulebot.repositories.WeekRepository
import com.ahmed.schedulebot.services.ImageService
import com.ahmed.schedulebot.services.ScheduleImageBuilder
import io.mockk.mockk
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.util.AssertionErrors.assertEquals

@SpringBootTest(
    classes = [ScheduleBotApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class ImageServiceTests {

    val scheduleEntryRepository= mockk<ScheduleEntryRepository>()
    val weekRepository= mockk<WeekRepository>()
    val scheduleImageBuilder= ScheduleImageBuilder()

    @Test
    fun givenNoScheduleData_whenCallingGetImage_thenNoImageIsCached() {
        // given:
        val arbitraryYear = 2024
        val arbitraryWeekNumber =1
        every { weekRepository.findByYearAndWeekNumber(arbitraryYear, arbitraryWeekNumber) } returns null
        every { scheduleEntryRepository.findByWeek(Week(arbitraryYear, arbitraryWeekNumber)) } returns null
        val imageService = ImageService(scheduleEntryRepository,weekRepository,scheduleImageBuilder)

        // when:
        val result: ByteArray = imageService.getImage(arbitraryYear, arbitraryWeekNumber)
        imageService.getImage(arbitraryYear, arbitraryWeekNumber)

        // then:
        assertEquals("The byte array from getImage", 0, result.size)
        //Verify that no caching has happened
        verify(exactly = 2) { weekRepository.findByYearAndWeekNumber(arbitraryYear, arbitraryWeekNumber) }
    }
}
