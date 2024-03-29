package com.ahmed.schedulebot.repositories

import com.ahmed.schedulebot.entities.Week
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface WeekRepository : CrudRepository<Week, Int> {
    fun findByYearAndWeekNumber(year: Int, weekNumber: Int): Week?
}
