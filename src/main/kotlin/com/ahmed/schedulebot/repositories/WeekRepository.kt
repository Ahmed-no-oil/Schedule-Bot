package com.ahmed.schedulebot.repositories

import com.ahmed.schedulebot.entities.Week
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface WeekRepository: CrudRepository<Week,Int> {
    fun findByWeekNumber(weekNumber: Int): Week?
}