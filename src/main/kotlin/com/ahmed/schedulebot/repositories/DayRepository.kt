package com.ahmed.schedulebot.repositories

import com.ahmed.schedulebot.entities.Day
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface DayRepository :CrudRepository<Day,Int>{
}